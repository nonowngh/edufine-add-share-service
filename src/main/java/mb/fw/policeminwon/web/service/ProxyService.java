package mb.fw.policeminwon.web.service;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.netty.buffer.Unpooled;
import mb.fw.policeminwon.constants.SystemCodeConstants;
import mb.fw.policeminwon.constants.TcpCommonSettingConstants;
import mb.fw.policeminwon.constants.TcpStatusCode;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.utils.ByteBufUtils;

@Service
@Profile("proxy")
public class ProxyService {

	private final String targetSystemCode = SystemCodeConstants.KFTC;

	private final AsyncConnectionClient client;

	public ProxyService(List<AsyncConnectionClient> clients) {
		client = clients.stream().filter(client -> client.getSystemCode().equals(targetSystemCode)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Tcp 클라이언트를 찾을 수 없습니다. 시스템 코드: " + targetSystemCode));
	}

	public void sendResponseViewBillingDetail(String tcpHeaderMessage, String tcpBodyMessage, String esbTransactionId)
			throws Exception {
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", TcpStatusCode.SUCCESS.getCode(), esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, TcpCommonSettingConstants.MESSAGE_CHARSET))));
	}

	public void sendResponsePaymentResultNotificaiton(String tcpHeaderMessage, String tcpBodyMessage,
			String esbTransactionId) throws Exception {
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", TcpStatusCode.SUCCESS.getCode(), esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, TcpCommonSettingConstants.MESSAGE_CHARSET))));
	}
	
	public void sendResponseCancelPayment(String tcpHeaderMessage, String tcpBodyMessage,
			String esbTransactionId) throws Exception {
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0430", TcpStatusCode.SUCCESS.getCode(), esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, TcpCommonSettingConstants.MESSAGE_CHARSET))));
	}

	public void sendResponseError(String tcpHeaderMessage, String statusCode, String esbTransactionId)
			throws Exception {
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.copiedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", statusCode, esbTransactionId))));
	}
}
