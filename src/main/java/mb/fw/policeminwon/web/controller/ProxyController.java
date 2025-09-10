package mb.fw.policeminwon.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mb.fw.policeminwon.web.dto.ESBApiRequest;
import mb.fw.policeminwon.web.service.ProxyService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/esb/api")
public class ProxyController {

	private final ProxyService proxyService;

	public ProxyController(ProxyService proxyService) {
		this.proxyService = proxyService;
	}

	@PostMapping("/proxy/view-billing-detail")
	public Mono<ResponseEntity<String>> viewBillingDetail(@RequestBody ESBApiRequest request) {
		return Mono.just(ResponseEntity.accepted().body("Accept summary(ViewBillingDetail) service call")).doOnSuccess(response -> {
			proxyService.sendResponseViewBillingDetail(request.getHeaderMessage(), request.getBodyMessage(),
					request.getTransactionId());
		});
	}
	
	@PostMapping("/proxy/payment-result-notification")
	public Mono<ResponseEntity<String>> paymentResultNotificaiton(@RequestBody ESBApiRequest request) {
		return Mono.just(ResponseEntity.accepted().body("Accept summary(PaymentResultNotificaiton) service call")).doOnSuccess(response -> {
			proxyService.sendResponsePaymentResultNotificaiton(request.getHeaderMessage(), request.getBodyMessage(),
					request.getTransactionId());
		});
	}

}
