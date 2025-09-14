package mb.fw.policeminwon.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.web.dto.ESBApiRequest;
import mb.fw.policeminwon.web.service.SummaryService;
import reactor.core.publisher.Mono;

@Profile("summary")
@RestController
@RequestMapping("/esb/api")
public class SummaryController {

	private final SummaryService summaryService;

	public SummaryController(SummaryService summaryService) {
		this.summaryService = summaryService;
	}

	@PostMapping("/summary" + ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL)
	public Mono<ResponseEntity<String>> viewBillingDetail(@RequestBody ESBApiRequest request) {
		summaryService.doAsyncViewBillingDetail(request);
		return Mono.just(ResponseEntity.accepted().body("Accept summary(ViewBillingDetail) service call [" + request.getTransactionId() + "]"));
	}
	
	@PostMapping("/summary" + ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION)
	public Mono<ResponseEntity<String>> paymentResultNotificaiton(@RequestBody ESBApiRequest request) {
		summaryService.doAsyncPaymentResultNotification(request);
		return Mono.just(ResponseEntity.accepted().body("Accept summary(paymentResultNotificaiton) service call [" + request.getTransactionId() + "]"));
	}

}
