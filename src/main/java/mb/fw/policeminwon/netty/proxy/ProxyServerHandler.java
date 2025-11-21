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

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import lombok.extern.slf4j.Slf4j;
import mb.fw.atb.util.TransactionIdGenerator;
import mb.fw.policeminwon.constants.SystemCodeConstants;
import mb.fw.policeminwon.constants.TcpCommonSettingConstants;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.constants.TcpMessageConstants;
import mb.fw.policeminwon.constants.TcpStatusCode;
import mb.fw.policeminwon.dto.CancelPaymentBody;
import mb.fw.policeminwon.dto.PaymentResultNotificationBody;
import mb.fw.policeminwon.dto.SummaryCommonMessage;
import mb.fw.policeminwon.dto.ViewBillingDetailBody;
import mb.fw.policeminwon.exception.CustomHandlerException;
import mb.fw.policeminwon.filter.TcpHandlerLoggingFilter;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CancelPaymentParser;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.parser.PaymentResultNotificationParser;
import mb.fw.policeminwon.parser.ViewBillingDetailParser;
import mb.fw.policeminwon.parser.slice.MessageSlice;
import mb.fw.policeminwon.spec.InterfaceSpec;
import mb.fw.policeminwon.spec.InterfaceSpecList;
import mb.fw.policeminwon.utils.ByteBufUtils;
import mb.fw.policeminwon.utils.CommonLoggingUtils;
import mb.fw.policeminwon.utils.TransactionSequenceGenerator;
import reactor.core.publisher.Mono;

@Slf4j
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

	private static ObjectMapper mapper = new ObjectMapper();

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
		inBuf.retain();
		try {
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
					if (SystemCodeConstants.KFTC.equals(sndCode))
						rcvCode = SystemCodeConstants.SUMMRAY;
					else
						sndCode = SystemCodeConstants.SUMMRAY;
				}
			}
			String responseCode = MessageSlice.getResponseCode(inBuf);
			InterfaceSpec interfaceSpec = interfaceSpecList.findInterfaceInfo(sndCode, rcvCode,
					tcpHeaderTransactionCode.getCode());
			String reqMsgDateTime = MessageSlice.getSendTime(inBuf);
			String nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
			String esbTxId = TransactionIdGenerator.generate(interfaceSpec.getInterfaceId(),
					TransactionSequenceGenerator.getNextSequence(), reqMsgDateTime, nowDateTime);
			String centerTxId = MessageSlice.getCenterTxId(inBuf);

			Map<TcpHeaderTransactionCode, Mono<Void>> actions = new HashMap<>();
			// 테스트 콜
			actions.put(TcpHeaderTransactionCode.TEST_CALL, testCall(inBuf, interfaceSpec));
			// 고지내역 상세조회
			actions.put(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL,
					penaltyProcess(inBuf, interfaceSpec, esbTxId, centerTxId));
			// 납부결과 통지
			actions.put(TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION,
					penaltyProcess(inBuf, interfaceSpec, esbTxId, centerTxId));
			// 납부 (재)취소
			actions.put(TcpHeaderTransactionCode.CANCEL_PAYMENT, cancelProcess(inBuf, interfaceSpec, esbTxId));

			Mono<Void> action = actions.getOrDefault(tcpHeaderTransactionCode, Mono.error(
					new IllegalArgumentException("Invalid transaction-code -> " + tcpHeaderTransactionCode.getCode())));
			Mono<Void> filteredAction = TcpHandlerLoggingFilter.routeLoggingFilter(action, interfaceSpec, jmsTemplate,
					esbTxId, nowDateTime, responseCode, inBuf);
			filteredAction.doFinally(signal -> {
				try {
					if (inBuf.refCnt() > 0)
						ReferenceCountUtil.release(inBuf);
				} catch (Exception ex) {
					log.error("Error while releasing inBuf", ex);
				}
			}).subscribe();
		} finally {
			if (((ReferenceCounted) msg).refCnt() > 0)
				ReferenceCountUtil.release(msg);
		}
	}

	private Mono<Void> cancelProcess(ByteBuf inBuf, InterfaceSpec interfaceSpec, String esbTxId) {
		return getTcpClientAndSendMessage(interfaceSpec.getRcvCode(), inBuf).then(Mono.defer(() -> {
			if (!SystemCodeConstants.KFTC.equals(interfaceSpec.getSndCode()))
				return Mono.empty();
			String useOrgTxId = esbTxId.substring(esbTxId.length() - 12);
			String bodyMessage = MessageSlice.getCancelPaymentTotalBody(inBuf);
			CancelPaymentBody reqBody = CancelPaymentParser.toEntity(bodyMessage);
			reqBody.setOrgaTranNo(useOrgTxId);
			return apiCall(reqBody, interfaceSpec.getApiPath()) // Map<String, Object>로 받도록 apiCall 수정 필요
					.flatMap(response -> {
						log.info("Api reponse -> [{}]'{}'", response.getResCode(), response.getResMsg());
						if (response.getOutputData() == null || response.getOutputData().isEmpty()) {
							return getTcpClientAndSendMessage(SystemCodeConstants.ESB,
									buildResponseBuf(inBuf, "0430", response.getResCode(), useOrgTxId, bodyMessage));
						}
						CancelPaymentBody resBody = mapper.convertValue(response.getOutputData(),
								CancelPaymentBody.class);
						String msg = CancelPaymentParser.toMessage(resBody);
						return getTcpClientAndSendMessage(SystemCodeConstants.ESB,
								buildResponseBuf(inBuf, "0430", response.getResCode(), useOrgTxId, msg));
					}).onErrorResume(ex -> {
						log.error("API 호출 중 오류 발생: " + ex.getMessage());
						ByteBuf outBuf = buildResponseBuf(inBuf, "0430", TcpStatusCode.SYSTEM_ERROR.getCode(),
								useOrgTxId, "시스템 오류 : 즉심 API 호출 오류");
						return getTcpClientAndSendMessage(SystemCodeConstants.ESB, outBuf);
					});
		}));
	}

	private Mono<Void> penaltyProcess(ByteBuf inBuf, InterfaceSpec interfaceSpec, String esbTxId, String centerTxId) {
		String rcvCode = interfaceSpec.getRcvCode();
		if (!SystemCodeConstants.SUMMRAY.equals(rcvCode)) {
			return getTcpClientAndSendMessage(rcvCode, inBuf);
		}
		String useOrgTxId = esbTxId.substring(esbTxId.length() - 12);
		if (TcpHeaderTransactionCode.VIEW_BILLING_DETAIL.getCode().equals(interfaceSpec.getMessageCode())) {
			String bodyMessage = MessageSlice.getVeiwBillingDetailTotalBody(inBuf);
			ViewBillingDetailBody reqBody = ViewBillingDetailParser.toEntity(bodyMessage);
			reqBody.setCentTranNo(centerTxId);
			reqBody.setOrgaTranNo(useOrgTxId);
			return apiCall(reqBody, interfaceSpec.getApiPath()) // Map<String, Object>로 받도록 apiCall 수정 필요
					.flatMap(response -> {
						log.info("Api reponse -> [{}]'{}'", response.getResCode(), response.getResMsg());
						if (response.getOutputData() == null || response.getOutputData().isEmpty()) {
							return getTcpClientAndSendMessage(SystemCodeConstants.ESB,
									buildResponseBuf(inBuf, "0210", response.getResCode(), useOrgTxId, bodyMessage));
						}
						ViewBillingDetailBody resBody = mapper.convertValue(response.getOutputData(),
								ViewBillingDetailBody.class);
						String msg = ViewBillingDetailParser.toMessage(resBody);
						return getTcpClientAndSendMessage(SystemCodeConstants.ESB,
								buildResponseBuf(inBuf, "0210", response.getResCode(), useOrgTxId, msg));
					}).onErrorResume(ex -> {
						log.error("API 호출 중 오류 발생: ", ex);
						ByteBuf outBuf = buildResponseBuf(inBuf, "0210", TcpStatusCode.SYSTEM_ERROR.getCode(),
								useOrgTxId, bodyMessage);
						return getTcpClientAndSendMessage(SystemCodeConstants.ESB, outBuf);
					});
		} else {
			String bodyMessage = MessageSlice.getPaymentResultNotificationTotalBody(inBuf);
			PaymentResultNotificationBody reqBody = PaymentResultNotificationParser.toEntity(bodyMessage);
			reqBody.setCentTranNo(centerTxId);
			reqBody.setOrgaTranNo(useOrgTxId);
			return apiCall(reqBody, interfaceSpec.getApiPath()) // Map<String, Object>로 받도록 apiCall 수정 필요
					.flatMap(response -> {
						log.info("Api reponse -> [{}]'{}'", response.getResCode(), response.getResMsg());
						if (response.getOutputData() == null || response.getOutputData().isEmpty()) {
							return getTcpClientAndSendMessage(SystemCodeConstants.ESB,
									buildResponseBuf(inBuf, "0210", response.getResCode(), useOrgTxId, bodyMessage));
						}
						PaymentResultNotificationBody resBody = mapper.convertValue(response.getOutputData(),
								PaymentResultNotificationBody.class);
						String msg = PaymentResultNotificationParser.toMessage(resBody);
						return getTcpClientAndSendMessage(SystemCodeConstants.ESB,
								buildResponseBuf(inBuf, "0210", response.getResCode(), useOrgTxId, msg));
					}).onErrorResume(ex -> {
						log.error("API 호출 중 오류 발생: ", ex);
						ByteBuf outBuf = buildResponseBuf(inBuf, "0210", TcpStatusCode.SYSTEM_ERROR.getCode(),
								useOrgTxId, bodyMessage);
						return getTcpClientAndSendMessage(SystemCodeConstants.ESB, outBuf);
					});
		}
	}

	private Mono<Void> testCall(ByteBuf inBuf, InterfaceSpec interfaceSpec) {
		if (directTestCallReturn != null) {
			ByteBuf buf = Unpooled.copiedBuffer(directTestCallReturn, TcpCommonSettingConstants.MESSAGE_CHARSET);
			return getTcpClientAndSendMessage(SystemCodeConstants.KFTC, buf);
		}
		return getTcpClientAndSendMessage(interfaceSpec.getRcvCode(), inBuf);
	}

//	public <T> Mono<SummaryCommonMessage<T>> apiCall(T request, String contextPath) {
//		if (webClient == null) {
//			log.error("WebClient is NULL. check yaml file.");
//			return Mono.error(new IllegalStateException("WebClient is NULL"));
//		}
//		return webClient.post().uri(contextPath).bodyValue(request).retrieve()
//				.bodyToMono(new ParameterizedTypeReference<SummaryCommonMessage<T>>() {
//				}).doOnNext(response -> log.info("API response : {}", response));
//	}

	public <T> Mono<SummaryCommonMessage<Map<String, Object>>> apiCall(T request, String contextPath) {
		if (webClient == null) {
			log.error("WebClient is NULL. check yaml file.");
			return Mono.error(new IllegalStateException("WebClient is NULL"));
		}
		return webClient.post().uri(contextPath).bodyValue(request).retrieve()
				.bodyToMono(new ParameterizedTypeReference<SummaryCommonMessage<Map<String, Object>>>() {
				}).doOnNext(response -> log.info("API response : {}", response));
	}

	private Mono<Void> getTcpClientAndSendMessage(String targetSystemCode, ByteBuf inBuf) {
		return Mono.fromCallable(() -> {
			AsyncConnectionClient asyncClient = clients.stream()
					.filter(client -> client.getSystemCode().equals(targetSystemCode)).findFirst()
					.orElseThrow(() -> new IllegalStateException("Tcp 클라이언트를 찾을 수 없습니다. 시스템 코드: " + targetSystemCode));
			asyncClient.callAsync(ByteBufUtils.addMessageLength(inBuf));
			return null; // Mono<Void>
		});
	}

	private ByteBuf buildResponseBuf(ByteBuf inBuf, String msgCode, String resCode, String orgaTxId,
			String messageBody) {
		ByteBuf headerBuf = null;
		ByteBuf bodyBuf = null;
		ByteBuf outBuf = null;
		try {
			headerBuf = CommonHeaderParser.responseHeader(inBuf, msgCode, resCode, orgaTxId);
			bodyBuf = Unpooled.copiedBuffer(messageBody, TcpCommonSettingConstants.MESSAGE_CHARSET);
			// headerBuf + bodyBuf 합친 새로운 outBuf
			outBuf = Unpooled.copiedBuffer(headerBuf, bodyBuf);
			return outBuf;
		} finally {
			if (headerBuf != null && headerBuf.refCnt() > 0)
				headerBuf.release();
			if (bodyBuf != null && bodyBuf.refCnt() > 0)
				bodyBuf.release();
		}
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
		log.error("Handle proxy server error! ->", cause);
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
