package mb.fw.policeminwon.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.netty.buffer.Unpooled;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.web.dto.ESBApiRequest;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/esb/api")
public class ProxyController {

	private final AsyncConnectionClient client;

	public ProxyController(AsyncConnectionClient client) {
		this.client = client;
	}

	@PostMapping("/proxy")
	public Mono<ResponseEntity<String>> summaryCall(@RequestBody ESBApiRequest request) {
		return Mono.just(ResponseEntity.accepted().body("Accept summary service call")).doOnSuccess(response -> {
			client.callAsync(Unpooled.copiedBuffer(request.getBodyData(), ByteEncodingConstants.CHARSET));
		});
	}

}
