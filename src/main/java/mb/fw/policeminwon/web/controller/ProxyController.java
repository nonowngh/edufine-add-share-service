package mb.fw.policeminwon.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.web.dto.ESBApiMessage;
import mb.fw.policeminwon.web.service.ProxyService;
import reactor.core.publisher.Mono;

@Profile("proxy")
@Slf4j
@RestController
@RequestMapping("/esb/api")
public class ProxyController {

	private final ProxyService proxyService;

	public ProxyController(ProxyService proxyService) {
		this.proxyService = proxyService;
	}

	@PostMapping("/proxy" + ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL)
	public Mono<ResponseEntity<String>> viewBillingDetail(@RequestBody ESBApiMessage request) {
		return Mono.just(ResponseEntity.accepted().body("Accept proxy(ViewBillingDetail) service call"))
				.doOnSuccess(response -> {
					try {
						proxyService.sendResponseViewBillingDetail(request.getHeaderMessage(), request.getBodyMessage(),
								request.getTransactionId());
					} catch (Exception e) {
						log.error("proxyService error!", e);
					}
				});
	}

	@PostMapping("/proxy" + ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION)
	public Mono<ResponseEntity<String>> paymentResultNotificaiton(@RequestBody ESBApiMessage request) {
		return Mono.just(ResponseEntity.accepted().body("Accept proxy(PaymentResultNotificaiton) service call"))
				.doOnSuccess(response -> {
					try {
						proxyService.sendResponsePaymentResultNotificaiton(request.getHeaderMessage(),
								request.getBodyMessage(), request.getTransactionId());
					} catch (Exception e) {
						log.error("proxyService error!", e);
					}
				});
	}

	@PostMapping("/proxy" + ESBAPIContextPathConstants.ERROR)
	public Mono<ResponseEntity<String>> error(@RequestBody ESBApiMessage message) {
		log.info("IN~~~~ proxy");
		return Mono.just(ResponseEntity.accepted().body("Accept proxy(Error) service call")).doOnSuccess(response -> {
			try {
				proxyService.sendResponseError(message.getHeaderMessage(), message.getStatusCode(),
						message.getTransactionId());
			} catch (Exception e) {
				log.error("proxyService error!", e);
			}
		});
	}

}
