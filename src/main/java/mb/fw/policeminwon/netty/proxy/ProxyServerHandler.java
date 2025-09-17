package mb.fw.policeminwon.netty.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.constants.TcpBodyConstatns;
import mb.fw.policeminwon.constants.TcpHeaderSrFlag;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.filter.TcpHandlerLoggingFilter;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.slice.MessageSlice;
import mb.fw.policeminwon.spec.InterfaceSpec;
import mb.fw.policeminwon.spec.InterfaceSpecList;
import mb.fw.policeminwon.web.dto.ESBApiMessage;
import reactor.core.publisher.Mono;

@Slf4j
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

	private final List<AsyncConnectionClient> clients;
	private final WebClient webClient;
	private final InterfaceSpecList interfaceSpecList;
	private final JmsTemplate jmsTemplate;
	private final String directTestCallReturn;

	public ProxyServerHandler(List<AsyncConnectionClient> clients, WebClient webClient,
			InterfaceSpecList interfaceSpecList, JmsTemplate jmsTemplate, String directTestCallReturn) {
		this.clients = clients;
		this.webClient = webClient;
		this.interfaceSpecList = interfaceSpecList;
		this.jmsTemplate = jmsTemplate;
		this.directTestCallReturn = directTestCallReturn;
	}

	private static final Set<TcpHeaderTransactionCode> panaltyTransactionCode = Collections.unmodifiableSet(
			new HashSet<TcpHeaderTransactionCode>(Arrays.asList(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL,
					TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION)));

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf inBuf = (ByteBuf) msg;
		try {
			String transactionCode = MessageSlice.getTransactionCode(inBuf);
			log.info("KFTC transaction-code -> [{}]", transactionCode);
			String srFlag = MessageSlice.getSrFlag(inBuf);
			String sndCode = TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstatns.KFTC
					: SystemCodeConstatns.TRAFFIC;
			String rcvCode = TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstatns.TRAFFIC
					: SystemCodeConstatns.KFTC;
			if (panaltyTransactionCode.contains(TcpHeaderTransactionCode.fromCode(transactionCode))) {
				String policeSystemCode = MessageSlice.getElecPayNo(inBuf);
				if (policeSystemCode.startsWith(TcpBodyConstatns.getSJSElecNumType())) {
					rcvCode = SystemCodeConstatns.SUMMRAY;
				}
			}
			InterfaceSpec interfaceSpec = interfaceSpecList.findInterfaceInfo(sndCode, rcvCode, transactionCode);

			Map<String, Mono<Void>> actions = new HashMap<>();
			// 테스트 콜
			actions.put(TcpHeaderTransactionCode.TEST_CALL.getCode(), testCall(inBuf, interfaceSpec));
			// 고지내역 상세조회
			actions.put(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL.getCode(), penaltyProcess(inBuf, interfaceSpec));
			// 납부결과 통지
			actions.put(TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION.getCode(),
					penaltyProcess(inBuf, interfaceSpec));
			// 납부 (재)취소
			actions.put(TcpHeaderTransactionCode.CANCEL_PAYMENT.getCode(), cancelProcess(inBuf, interfaceSpec));

			Mono<Void> action = actions.getOrDefault(transactionCode,
					Mono.error(new IllegalArgumentException("Invalid transaction-code -> " + transactionCode)));
			Mono<Void> filteredAction = TcpHandlerLoggingFilter.routeLoggingFilter(action, interfaceSpec, jmsTemplate);
			filteredAction.subscribe();
		} finally {
			if (((ReferenceCounted) msg).refCnt() > 0)
				ReferenceCountUtil.release(msg);
		}
	}

	private Mono<Void> cancelProcess(ByteBuf inBuf, InterfaceSpec interfaceSpec) {
		return Mono.fromRunnable(() -> getTcpClientAndSendMessage(interfaceSpec.getRcvCode(), inBuf))
				.then(SystemCodeConstatns.KFTC.equals(interfaceSpec.getSndCode()) ? esbApiCall(
						MessageSlice.getHeaderMessage(inBuf), MessageSlice.getCancelPaymentTotalBody((inBuf)),
						ESBAPIContextPathConstants.CANCEL_PAYMENT) : Mono.empty());
	}

	private Mono<Void> penaltyProcess(ByteBuf inBuf, InterfaceSpec interfaceSpec) {
		String rcvCode = interfaceSpec.getRcvCode();
		if (SystemCodeConstatns.SUMMRAY.equals(rcvCode)) {
			// 고지내역 상세조회
			if (TcpHeaderTransactionCode.VIEW_BILLING_DETAIL.getCode().equals(interfaceSpec.getMessageCode()))
				return esbApiCall(MessageSlice.getHeaderMessage(inBuf),
						MessageSlice.getVeiwBillingDetailTotalBody((inBuf)),
						ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL);
			// 납부결과 통지
			else
				return esbApiCall(MessageSlice.getHeaderMessage(inBuf),
						MessageSlice.getPaymentResultNotificationTotalBody((inBuf)),
						ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION);
		} else {
			return getTcpClientAndSendMessage(rcvCode, inBuf);
		}
	}

	private Mono<Void> testCall(ByteBuf inBuf, InterfaceSpec interfaceSpec) {
			if (directTestCallReturn != null) {
				return  getTcpClientAndSendMessage(SystemCodeConstatns.KFTC,
						Unpooled.copiedBuffer(directTestCallReturn, ByteEncodingConstants.CHARSET));
			} else {
				return getTcpClientAndSendMessage(interfaceSpec.getRcvCode(), inBuf);
			}
	}

	public Mono<Void> esbApiCall(String header, String body, String contextPath) {
		if (webClient == null) {
			log.error("WebClient is NULL. check yaml file.");
			return Mono.error(new IllegalStateException("WebClient is NULL"));
		}

		return webClient.post().uri(contextPath)
				.bodyValue(ESBApiMessage.builder().headerMessage(header).bodyMessage(body).build()).retrieve()
				.bodyToMono(String.class).doOnNext(response -> log.info("API 응답: {}", response)).then();
	}

	private Mono<Void> getTcpClientAndSendMessage(String targetSystemCode, ByteBuf inBuf) {
		return Mono.fromCallable(() -> {
			AsyncConnectionClient asyncClient = clients.stream()
					.filter(client -> client.getSystemCode().equals(targetSystemCode)).findFirst()
					.orElseThrow(() -> new IllegalStateException("Tcp 클라이언트를 찾을 수 없습니다. 시스템 코드: " + targetSystemCode));
			asyncClient.callAsync(inBuf);
			return null; // Mono<Void>
		});

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

}
