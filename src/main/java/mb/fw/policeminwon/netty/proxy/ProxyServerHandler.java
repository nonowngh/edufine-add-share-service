package mb.fw.policeminwon.netty.proxy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import mb.fw.atb.util.TransactionIdGenerator;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.constants.SystemCodeConstants;
import mb.fw.policeminwon.constants.TcpCommonSettingConstants;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.constants.TcpMessageConstants;
import mb.fw.policeminwon.constants.TcpStatusCode;
import mb.fw.policeminwon.exception.CustomHandlerException;
import mb.fw.policeminwon.filter.TcpHandlerLoggingFilter;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.parser.slice.MessageSlice;
import mb.fw.policeminwon.spec.InterfaceSpec;
import mb.fw.policeminwon.spec.InterfaceSpecList;
import mb.fw.policeminwon.utils.ByteBufUtils;
import mb.fw.policeminwon.utils.CommonLoggingUtils;
import mb.fw.policeminwon.utils.TransactionSequenceGenerator;
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
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf inBuf = (ByteBuf) msg;
		try {
//			String transactionCode = MessageSlice.getTransactionCode(inBuf);
			TcpHeaderTransactionCode tcpHeaderTransactionCode = TcpHeaderTransactionCode
					.fromCode(MessageSlice.getTransactionCode(inBuf));
			if (tcpHeaderTransactionCode == null)
				throw new CustomHandlerException("Invalid transaction-code: " + MessageSlice.getTransactionCode(inBuf),
						TcpStatusCode.FORMAT_ERROR, SystemCodeConstants.KFTC, MessageSlice.getHeaderMessage(inBuf));
			log.info("KFTC transaction-code -> [{}]", tcpHeaderTransactionCode.getCode());
			String srFlag = MessageSlice.getSrFlag(inBuf);
			String sndCode = TcpMessageConstants.SRFLAG_KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstants.KFTC
					: SystemCodeConstants.TRAFFIC;
			String rcvCode = TcpMessageConstants.SRFLAG_KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstants.TRAFFIC
					: SystemCodeConstants.KFTC;
			if (panaltyTransactionCode.contains(tcpHeaderTransactionCode)) {
				String policeSystemCode = MessageSlice.getElecPayNo(inBuf);
				if (policeSystemCode.startsWith(TcpMessageConstants.getSJSElecNumType())) {
					rcvCode = SystemCodeConstants.SUMMRAY;
				}
			}
			String responseCode = MessageSlice.getResponseCode(inBuf);
			InterfaceSpec interfaceSpec = interfaceSpecList.findInterfaceInfo(sndCode, rcvCode,
					tcpHeaderTransactionCode.getCode());
			String nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
			String esbTxId = TransactionIdGenerator.generate(interfaceSpec.getInterfaceId(),
					TransactionSequenceGenerator.getNextSequence(), nowDateTime);

			// 비동기로 인한 요청/응답 전문 짝을 맞추기 위해 '센터전문관리' 항목에 트랜젝션 아이디 뒤에서 12자리 짤라서 넘김
			if (!rcvCode.equals(SystemCodeConstants.KFTC)) {
				byte[] bytes = esbTxId.getBytes(TcpCommonSettingConstants.MESSAGE_CHARSET);
				int start = Math.max(0, bytes.length - 12);
				byte[] tailBytes = Arrays.copyOfRange(bytes, start, bytes.length);
				ByteBuf txIdBuf = Unpooled.wrappedBuffer(tailBytes);
				inBuf.setBytes(51, txIdBuf, 12);
			}

			Map<TcpHeaderTransactionCode, Mono<Void>> actions = new HashMap<>();
			// 테스트 콜
			actions.put(TcpHeaderTransactionCode.TEST_CALL, testCall(inBuf, interfaceSpec));
			// 고지내역 상세조회
			actions.put(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL, penaltyProcess(inBuf, interfaceSpec, esbTxId));
			// 납부결과 통지
			actions.put(TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION,
					penaltyProcess(inBuf, interfaceSpec, esbTxId));
			// 납부 (재)취소
			actions.put(TcpHeaderTransactionCode.CANCEL_PAYMENT, cancelProcess(inBuf, interfaceSpec, esbTxId));

			
			Mono<Void> action = actions.getOrDefault(tcpHeaderTransactionCode, Mono.error(
					new IllegalArgumentException("Invalid transaction-code -> " + tcpHeaderTransactionCode.getCode())));
			Mono<Void> filteredAction = TcpHandlerLoggingFilter.routeLoggingFilter(action, interfaceSpec, jmsTemplate,
					esbTxId, nowDateTime, responseCode);
			filteredAction.subscribe();
		} finally {
			if (((ReferenceCounted) msg).refCnt() > 0)
				ReferenceCountUtil.release(msg);
		}
	}

	private Mono<Void> cancelProcess(ByteBuf inBuf, InterfaceSpec interfaceSpec, String esbTxId) {
		return Mono.fromRunnable(() -> getTcpClientAndSendMessage(interfaceSpec.getRcvCode(), inBuf))
				.then(SystemCodeConstants.KFTC.equals(interfaceSpec.getSndCode())
						? esbApiCall(MessageSlice.getHeaderMessage(inBuf),
								MessageSlice.getCancelPaymentTotalBody((inBuf)),
								ESBAPIContextPathConstants.CANCEL_PAYMENT, interfaceSpec.getInterfaceId(), esbTxId)
						: Mono.empty());
	}

	private Mono<Void> penaltyProcess(ByteBuf inBuf, InterfaceSpec interfaceSpec, String esbTxId) {
		String rcvCode = interfaceSpec.getRcvCode();
		if (SystemCodeConstants.SUMMRAY.equals(rcvCode)) {
			// 고지내역 상세조회
			if (TcpHeaderTransactionCode.VIEW_BILLING_DETAIL.getCode().equals(interfaceSpec.getMessageCode()))
				return esbApiCall(MessageSlice.getHeaderMessage(inBuf),
						MessageSlice.getVeiwBillingDetailTotalBody((inBuf)),
						ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL, interfaceSpec.getInterfaceId(), esbTxId);
			// 납부결과 통지
			else
				return esbApiCall(MessageSlice.getHeaderMessage(inBuf),
						MessageSlice.getPaymentResultNotificationTotalBody((inBuf)),
						ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION, interfaceSpec.getInterfaceId(),
						esbTxId);
		} else {
			return getTcpClientAndSendMessage(rcvCode, inBuf);
		}
	}

	private Mono<Void> testCall(ByteBuf inBuf, InterfaceSpec interfaceSpec) {
		if (directTestCallReturn != null) {
			return getTcpClientAndSendMessage(SystemCodeConstants.KFTC,
					Unpooled.copiedBuffer(directTestCallReturn, TcpCommonSettingConstants.MESSAGE_CHARSET));
		} else {
			return getTcpClientAndSendMessage(interfaceSpec.getRcvCode(), inBuf);
		}
	}

	public Mono<Void> esbApiCall(String header, String body, String contextPath, String interfaceId, String esbTxId) {
		if (webClient == null) {
			log.error("WebClient is NULL. check yaml file.");
			return Mono.error(new IllegalStateException("WebClient is NULL"));
		}

		return webClient.post().uri(contextPath)
				.bodyValue(ESBApiMessage.builder().interfaceId(interfaceId).transactionId(esbTxId).headerMessage(header)
						.bodyMessage(body).build())
				.retrieve().bodyToMono(String.class).doOnNext(response -> log.info("ESB API response : {}", response))
				.then();
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
		log.error("Handle proxy server error! -> {}", cause.getMessage());
		if (cause instanceof CustomHandlerException) {
			CustomHandlerException ex = (CustomHandlerException) cause;
			String targetSystemCode = ex.getSystemCode();
			String tcpHeaderMessage = ex.getHeaderMessage();
			CommonLoggingUtils.loggingTcpResponse(ex.getStatusCode().getCode());
			getTcpClientAndSendMessage(targetSystemCode,
					ByteBufUtils.addMessageLength(Unpooled.copiedBuffer(
							CommonHeaderParser.responseHeader(tcpHeaderMessage, "", ex.getStatusCode().getCode(), ""))))
					.subscribe();
		}
	}

}
