package mb.fw.policeminwon.netty.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.constants.TcpBodyConstatns;
import mb.fw.policeminwon.constants.TcpHeaderSrFlag;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.slice.MessageSlice;
import mb.fw.policeminwon.utils.LogUtils;
import mb.fw.policeminwon.web.dto.ESBApiRequest;

@Slf4j
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

	private final List<AsyncConnectionClient> clients;
	private final WebClient webClient;

	public ProxyServerHandler(List<AsyncConnectionClient> clients, WebClient webClient) {
		this.clients = clients;
		this.webClient = webClient;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf inBuf = (ByteBuf) msg;
		try {
			String transactionCode = MessageSlice.getTransactionCode(inBuf);
			log.info("transactionCode -> [{}]", transactionCode);
			String srFlag = MessageSlice.getSrFlag(inBuf);
			String targetSystemCode = TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstatns.TRAFFIC
					: SystemCodeConstatns.KFTC;

			Map<String, Runnable> actions = new HashMap<>();
			// 테스트 콜
			actions.put(TcpHeaderTransactionCode.TEST_CALL,
					() -> testCall(inBuf, srFlag, transactionCode, targetSystemCode));
			// 고지내역 상세조회
			actions.put(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL,
					() -> penaltyProcess(inBuf, srFlag, transactionCode, true, targetSystemCode));
			// 납부결과 통지
			actions.put(TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION,
					() -> penaltyProcess(inBuf, srFlag, transactionCode, false, targetSystemCode));
			// 납부 (재)취소
			actions.put(TcpHeaderTransactionCode.CANCEL_PAYMENT,
					() -> cancelProcess(inBuf, srFlag, transactionCode, targetSystemCode));

			actions.getOrDefault(transactionCode, () -> {
				throw new IllegalArgumentException("Invalid transaction-code -> " + transactionCode);
			}).run();

		} finally {
			if (((ReferenceCounted) msg).refCnt() > 0)
				ReferenceCountUtil.release(msg);
		}
	}

	private void cancelProcess(ByteBuf inBuf, String srFlag, String transactionCode, String targetSystemCode) {
		LogUtils.loggingLouteInfo(transactionCode, srFlag, false);

		getTcpClientAndSendMessage(targetSystemCode, inBuf);
		if (!TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag)) {
			esbApiCall(MessageSlice.getHeaderMessage(inBuf), MessageSlice.getCancelPaymentTotalBody((inBuf)),
					ESBAPIContextPathConstants.CANCEL_PAYMENT);
		}
	}

	private void penaltyProcess(ByteBuf inBuf, String srFlag, String transactionCode, boolean isBillingDetail,
			String targetSystemCode) {
		String policeSystemCode = MessageSlice.getElecPayNo(inBuf);

		if (policeSystemCode.startsWith(TcpBodyConstatns.getSJSElecNumType())) {
			LogUtils.loggingLouteInfo(transactionCode, srFlag, true);
			if (isBillingDetail)
				esbApiCall(MessageSlice.getHeaderMessage(inBuf), MessageSlice.getVeiwBillingDetailTotalBody((inBuf)),
						ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL);
			else
				esbApiCall(MessageSlice.getHeaderMessage(inBuf),
						MessageSlice.getPaymentResultNotificationTotalBody((inBuf)),
						ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION);
		} else {
			LogUtils.loggingLouteInfo(transactionCode, srFlag, false);
			getTcpClientAndSendMessage(targetSystemCode, inBuf);
		}
	}

	private void testCall(ByteBuf inBuf, String srFlag, String transactionCode, String targetSystemCode) {
		LogUtils.loggingLouteInfo(transactionCode, srFlag, false);
		getTcpClientAndSendMessage(targetSystemCode, inBuf);
	}

	private void esbApiCall(String header, String body, String contextPath) {
		if (webClient != null) {
			webClient.post().uri(contextPath)
					.bodyValue(ESBApiRequest.builder().headerMessage(header).bodyMessage(body).build()).retrieve()
					.bodyToMono(String.class).doOnNext(response -> {
						log.info("API 응답: " + response);
					}).doOnError(error -> {
						log.error("API 오류: " + error.getMessage());
					}).subscribe();
		} else {
			log.error("WebClient is NULL. check yaml file.");
		}
	}

	private void getTcpClientAndSendMessage(String targetSystemCode, ByteBuf inBuf) {
		AsyncConnectionClient asyncClient = clients.stream()
				.filter(client -> client.getSystemCode().equals(targetSystemCode)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Tcp 클라이언트를 찾을 수 없습니다. 시스템 코드: " + targetSystemCode));
		asyncClient.callAsync(inBuf);
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
