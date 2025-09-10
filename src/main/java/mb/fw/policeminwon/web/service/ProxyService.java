package mb.fw.policeminwon.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.netty.buffer.Unpooled;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.constants.TcpHeaderSrFlag;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.utils.ByteBufUtils;
import mb.fw.policeminwon.utils.LogUtils;

@Service
public class ProxyService {

	private final String targetSystemCode = SystemCodeConstatns.KFTC;

	private final AsyncConnectionClient client;

	public ProxyService(List<AsyncConnectionClient> clients) {
		client = clients.stream().filter(client -> client.getSystemCode().equals(targetSystemCode)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Tcp 클라이언트를 찾을 수 없습니다. 시스템 코드: " + targetSystemCode));
	}

	public void sendResponseViewBillingDetail(String tcpHeaderMessage, String tcpBodyMessage, String esbTransactionId) {
		LogUtils.loggingLouteInfo(TcpHeaderTransactionCode.VIEW_BILLING_DETAIL, TcpHeaderSrFlag.POLICE, true);
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", "000", esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, ByteEncodingConstants.CHARSET))));
	}
	
	public void sendResponsePaymentResultNotificaiton(String tcpHeaderMessage, String tcpBodyMessage, String esbTransactionId) {
		LogUtils.loggingLouteInfo(TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION, TcpHeaderSrFlag.POLICE, true);
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", "000", esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, ByteEncodingConstants.CHARSET))));
	}
}
