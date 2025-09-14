package mb.fw.policeminwon.web.service;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.utils.ByteBufUtils;

@Slf4j
@Service
@Profile("proxy")
public class ProxyService {

	private final String targetSystemCode = SystemCodeConstatns.KFTC;

	private final AsyncConnectionClient client;

	public ProxyService(List<AsyncConnectionClient> clients) {
		client = clients.stream().filter(client -> client.getSystemCode().equals(targetSystemCode)).findFirst()
				.orElse(null);
		if(client == null) log.error("사용가능한 'AsyncConnectionClient'가 없습니다.");
//		() -> new IllegalStateException("Tcp 클라이언트를 찾을 수 없습니다. 시스템 코드: " + targetSystemCode));
	}

	public void sendResponseViewBillingDetail(String tcpHeaderMessage, String tcpBodyMessage, String esbTransactionId) {
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", "000", esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, ByteEncodingConstants.CHARSET))));
	}
	
	public void sendResponsePaymentResultNotificaiton(String tcpHeaderMessage, String tcpBodyMessage, String esbTransactionId) {
		client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
				CommonHeaderParser.responseHeader(tcpHeaderMessage, "0210", "000", esbTransactionId),
				Unpooled.copiedBuffer(tcpBodyMessage, ByteEncodingConstants.CHARSET))));
	}
}
