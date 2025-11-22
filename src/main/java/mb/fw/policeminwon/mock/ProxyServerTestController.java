package mb.fw.policeminwon.mock;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.SystemCodeConstants;
import mb.fw.policeminwon.constants.TcpCommonSettingConstants;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.utils.ByteBufUtils;
import reactor.core.publisher.Mono;

@Profile("message-simul")
@Slf4j
@RestController
@RequestMapping("/proxy-test")
public class ProxyServerTestController {


	private final List<AsyncConnectionClient> clients;

	public ProxyServerTestController(List<AsyncConnectionClient> clients) {
		this.clients = clients;
	}
	@PostMapping(value = "/call", produces = "application/json; charset=UTF-8")
	public String simulateViewBillngDetail(@RequestBody Map<String, Object> requestBody) {
		log.info("Received request: " + requestBody);
		String reqMessage = (String) requestBody.get("message");
		getTcpClientAndSendMessage(SystemCodeConstants.ESB,
				Unpooled.copiedBuffer(reqMessage, TcpCommonSettingConstants.MESSAGE_CHARSET)).subscribe();
		return "전송완료";
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
	public static void main(String[] args) {
		Charset charset = Charset.forName("MS949"); // TcpCommonSettingConstants.MESSAGE_CHARSET 사용 가능
		String message = "IGN0990800122001   C   2025072909400CT123465678            121234567  62042116547120175980137202530000015506          000000000075000201807133650000                              6204211654712                    V Q         ";
		// String → ByteBuf
		ByteBuf buf = Unpooled.copiedBuffer(message, charset);
		// ByteBuf 읽기
		System.out.println(ByteBufUtils.addMessageLength(buf).toString(charset));
//        String readMessage = buf.toString(charset);
		buf.release();
	}
}
