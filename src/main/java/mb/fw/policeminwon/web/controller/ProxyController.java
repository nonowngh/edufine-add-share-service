package mb.fw.policeminwon.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.utils.ByteBufUtils;
import mb.fw.policeminwon.web.dto.ESBApiRequest;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/esb/api")
public class ProxyController {

	private final AsyncConnectionClient client;

	public ProxyController(AsyncConnectionClient client) {
		this.client = client;
	}

	@PostMapping("/proxy")
	public Mono<ResponseEntity<String>> summaryCall(@RequestBody ESBApiRequest request) {
		log.info("고지내역 상세조회 응답 - tcp send...[{}] -> [{}]", "즉심(SJS)", "금결원(KTF)");
		return Mono.just(ResponseEntity.accepted().body("Accept summary service call")).doOnSuccess(response -> {
			client.callAsync(ByteBufUtils.addMessageLength(Unpooled.wrappedBuffer(
					CommonHeaderParser.responseHeader(request.getHeaderMessage(), "0210", "000",
							request.getTransactionId()),
					Unpooled.copiedBuffer(request.getBodyMessage(), ByteEncodingConstants.CHARSET))));
		});
	}

}
