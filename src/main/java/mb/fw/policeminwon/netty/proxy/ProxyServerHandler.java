package mb.fw.policeminwon.netty.proxy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.TcpHeaderSrFlag;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.BodyCompareParser;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.parser.slice.MessageSlice;
import mb.fw.policeminwon.utils.ByteBufUtils;
import mb.fw.policeminwon.web.dto.ESBApiRequest;

@Slf4j
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

	private final AsyncConnectionClient client;
	private final WebClient webClient;

	public ProxyServerHandler(AsyncConnectionClient client, WebClient webClient) {
		this.client = client;
		this.webClient = webClient;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf inBuf = (ByteBuf) msg;
		try {
			String transactionCode = MessageSlice.getTransactionCode(inBuf);
			log.info("transactionCode -> [{}]", transactionCode);
			String srFlag = MessageSlice.getSrFlag(inBuf);

			Map<String, Runnable> actions = new HashMap<>();
			// 테스트 콜
			actions.put(TcpHeaderTransactionCode.TEST_CALL, () -> testCall(inBuf));
			// 고지내역 상세조회
			actions.put(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL, () -> veiwBillingDetail(inBuf, srFlag));
			// 납부결과 통지
			actions.put(TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION, () -> testCall(inBuf));
			// 납부 (재)취소
			actions.put(TcpHeaderTransactionCode.CANCEL_PAYMENT, () -> testCall(inBuf));

			actions.getOrDefault(transactionCode, () -> {
				throw new IllegalArgumentException("Invalid transaction-code -> " + transactionCode);
			}).run();

		} finally {
			if (((ReferenceCounted) msg).refCnt() > 0)
				ReferenceCountUtil.release(msg);
		}
	}

	private void veiwBillingDetail(ByteBuf inBuf, String srFlag) {
		String policeSystemCode = MessageSlice.getElecPayNo(inBuf);
		if (policeSystemCode.startsWith(BodyCompareParser.getSJSElctNum())) {
			if (TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag)) {
				log.info("고지내역 상세조회...[{}] -> [{}]", "금결원", "즉심(SJS)");
				esbApiCall(MessageSlice.getHeaderMessage(inBuf), MessageSlice.getVeiwBillingDetailTotalBody((inBuf)));
			} else {
				log.info("고지내역 상세조회...[{}] -> [{}]", "즉심(SJS)", "금결원");
				client.callAsync(inBuf);
			}
		} else {
			if (TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag)) {
				log.info("고지내역 상세조회 - BYPASS...[{}] -> [{}]", "금결원", "교통(TCS)");
			} else {
				log.info("고지내역 상세조회 - BYPASS...[{}] -> [{}]", "교통(TCS)", "금결원");
			}
			client.callAsync(inBuf);
		}
	}

	private void testCall(ByteBuf inBuf) {
//		log.info("테스트 콜...[{}] -> [{}]", "금결원", "프록시");
//		if(client.getBypassTestCall()) client.callAsync(inBuf);
//		else {
//		ByteBuf outBuf = ByteBufUtils
//				.addMessageLength(CommonHeaderParser.responseHeader(inBuf, "0810", "000", "testcall0123"));
//		client.callAsync(outBuf);
//		}
		
	    log.info("테스트 콜...[{}] -> [{}]", "금결원", "프록시");	    
	    Runnable callAsync = () -> {
	        ByteBuf outBuf = client.getBypassTestCall()
	                ? inBuf
	                : ByteBufUtils.addMessageLength(CommonHeaderParser.responseHeader(inBuf, "0810", "000", "testcall0123"));
	        
	        client.callAsync(outBuf);
	    };	    
	    callAsync.run();
	}

	private void esbApiCall(String header, String body) {
		if (webClient != null) {
			webClient.post().bodyValue(ESBApiRequest.builder().headerMessage(header).bodyMessage(body).build())
					.retrieve().bodyToMono(String.class).doOnNext(response -> {
						log.info("API 응답: " + response);
					}).doOnError(error -> {
						log.error("API 오류: " + error.getMessage());
					}).subscribe();
		} else {
			log.error("WebClient is NULL. check yaml file.");
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
		super.exceptionCaught(ctx, cause);
	}

}
