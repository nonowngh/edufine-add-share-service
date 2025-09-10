package mb.fw.policeminwon.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
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

	@PostMapping("/proxy" + ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL)
	public Mono<ResponseEntity<String>> viewBillingDetail(@RequestBody ESBApiRequest request) {
		return Mono.just(ResponseEntity.accepted().body("Accept proxy(ViewBillingDetail) service call")).doOnSuccess(response -> {
			proxyService.sendResponseViewBillingDetail(request.getHeaderMessage(), request.getBodyMessage(),
					request.getTransactionId());
		});
	}
	
	@PostMapping("/proxy" + ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION)
	public Mono<ResponseEntity<String>> paymentResultNotificaiton(@RequestBody ESBApiRequest request) {
		return Mono.just(ResponseEntity.accepted().body("Accept proxy(PaymentResultNotificaiton) service call")).doOnSuccess(response -> {
			proxyService.sendResponsePaymentResultNotificaiton(request.getHeaderMessage(), request.getBodyMessage(),
					request.getTransactionId());
		});
	}

}
