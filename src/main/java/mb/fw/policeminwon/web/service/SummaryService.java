package mb.fw.policeminwon.web.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.entity.PaymentResultNotificationEntity;
import mb.fw.policeminwon.entity.ViewBillingDetailEntity;
import mb.fw.policeminwon.parser.PaymentResultNotificationParser;
import mb.fw.policeminwon.parser.ViewBillingDetailParser;
import mb.fw.policeminwon.web.dto.ESBApiRequest;
import mb.fw.policeminwon.web.mapper.PaymentResultNotificationMapper;
import mb.fw.policeminwon.web.mapper.ViewBillingDetailMapper;

@lombok.extern.slf4j.Slf4j
@Service
public class SummaryService {

	private final WebClient callBackWebClient;

	private final ViewBillingDetailMapper viewBillingDetailMapper;
	private final PaymentResultNotificationMapper paymentResultNotificationMapper;

	public SummaryService(ViewBillingDetailMapper viewBillingDetailMapper,
			@Qualifier("callBackWebClient") WebClient callBackWebClient,
			PaymentResultNotificationMapper paymentResultNotificationMapper) {
		this.viewBillingDetailMapper = viewBillingDetailMapper;
		this.callBackWebClient = callBackWebClient;
		this.paymentResultNotificationMapper = paymentResultNotificationMapper;
	}

	public void doAsyncViewBillingDetail(ESBApiRequest request) {
		String elecPayNo = ViewBillingDetailParser.toEntity(request.getBodyMessage()).getElecPayNo();
		ViewBillingDetailEntity entity = Optional
				.ofNullable(viewBillingDetailMapper.selectBillingDetailByElecPayNo(elecPayNo)).orElseThrow(() -> {
					return new NoSuchElementException("elecPayNo '" + elecPayNo + "' 해당하는 정보가 없음");
				});
		runAsync(request, ViewBillingDetailParser.toMessage(entity), ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL);
	}

	public void doAsyncPaymentResultNotification(ESBApiRequest request) {
		PaymentResultNotificationEntity entity = PaymentResultNotificationParser.toEntity(request.getBodyMessage());
		paymentResultNotificationMapper.insertPaymentResultNotification(entity);
		runAsync(request, PaymentResultNotificationParser.toMessage(entity), ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION);
	}

	private void runAsync(ESBApiRequest request, String returnMessage, String contextPath) {
		CompletableFuture.runAsync(() -> {
			request.setBodyMessage(returnMessage);
			callBackWebClient.post().uri(contextPath).contentType(MediaType.APPLICATION_JSON).bodyValue(request).retrieve()
					.toBodilessEntity().doOnSuccess(r -> log.info("Callback success"))
					.doOnError(e -> log.error("Callback failed", e)).subscribe();
		});
	}

}
