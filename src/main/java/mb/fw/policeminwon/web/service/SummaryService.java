package mb.fw.policeminwon.web.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.constants.SystemCodeConstants;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;
import mb.fw.policeminwon.constants.TcpStatusCode;
import mb.fw.policeminwon.entity.PaymentResultNotificationEntity;
import mb.fw.policeminwon.entity.ViewBillingDetailEntity;
import mb.fw.policeminwon.exception.CustomServletException;
import mb.fw.policeminwon.parser.PaymentResultNotificationParser;
import mb.fw.policeminwon.parser.ViewBillingDetailParser;
import mb.fw.policeminwon.spec.InterfaceSpec;
import mb.fw.policeminwon.spec.InterfaceSpecList;
import mb.fw.policeminwon.web.dto.ESBApiMessage;
import mb.fw.policeminwon.web.mapper.PaymentResultNotificationMapper;
import mb.fw.policeminwon.web.mapper.ViewBillingDetailMapper;

@lombok.extern.slf4j.Slf4j
@Service
@Profile("summary")
public class SummaryService {

	private final WebClient callBackWebClient;

	private final ViewBillingDetailMapper viewBillingDetailMapper;
	private final PaymentResultNotificationMapper paymentResultNotificationMapper;
	private final InterfaceSpecList interfaceSpecList;

	public SummaryService(ViewBillingDetailMapper viewBillingDetailMapper,
			@Qualifier("callBackWebClient") WebClient callBackWebClient,
			PaymentResultNotificationMapper paymentResultNotificationMapper, InterfaceSpecList interfaceSpecList) {
		this.viewBillingDetailMapper = viewBillingDetailMapper;
		this.callBackWebClient = callBackWebClient;
		this.paymentResultNotificationMapper = paymentResultNotificationMapper;
		this.interfaceSpecList = interfaceSpecList;
	}

	private final String sndCode = SystemCodeConstants.SUMMRAY;
	private final String rcvCode = SystemCodeConstants.KFTC;

	public void doAsyncViewBillingDetail(ESBApiMessage apiMessage) {
		setResponseApiMessage(apiMessage, TcpHeaderTransactionCode.VIEW_BILLING_DETAIL);
		try {
			String elecPayNo = ViewBillingDetailParser.toEntity(apiMessage.getBodyMessage()).getElecPayNo();
			ViewBillingDetailEntity entity = Optional
					.ofNullable(viewBillingDetailMapper.selectBillingDetailByElecPayNo(elecPayNo))
					.orElseThrow(() -> new CustomServletException("elecPayNo '" + elecPayNo + "' 해당하는 정보가 없음", apiMessage,
							TcpStatusCode.NO_BILLING_RECORDS));
			log.info("Mapper[viewBillingDetailMapper] run completed.  -> parameter : {}", elecPayNo);
			runAsync(apiMessage, ViewBillingDetailParser.toMessage(entity),
					ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL);
		} catch (CustomServletException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomServletException(e.getMessage(), apiMessage, TcpStatusCode.SYSTEM_ERROR);
		}
	}

	public void doAsyncPaymentResultNotification(ESBApiMessage apiMessage) {
		setResponseApiMessage(apiMessage, TcpHeaderTransactionCode.VIEW_BILLING_DETAIL);
		try {
			PaymentResultNotificationEntity entity = PaymentResultNotificationParser.toEntity(apiMessage.getBodyMessage());
			paymentResultNotificationMapper.insertPaymentResultNotification(entity);
			log.info("Mapper[paymentResultNotificationMapper] run completed.  -> main parameter : {}", entity.getElecPayNo());
			runAsync(apiMessage, PaymentResultNotificationParser.toMessage(entity),
					ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION);
		} catch (CustomServletException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomServletException(e.getMessage(), apiMessage, TcpStatusCode.SYSTEM_ERROR);
		}
	}

	private void runAsync(ESBApiMessage request, String returnMessage, String contextPath) {
		CompletableFuture.runAsync(() -> {
			request.setBodyMessage(returnMessage);
			callBackWebClient.post().uri(contextPath).contentType(MediaType.APPLICATION_JSON).bodyValue(request)
					.retrieve().toBodilessEntity().doOnSuccess(r -> log.info("Callback success"))
					.doOnError(e -> log.error("Callback failed", e)).subscribe();
		});
	}

	private void setResponseApiMessage(ESBApiMessage apiMessage, TcpHeaderTransactionCode transactionCode) {
		InterfaceSpec interfaceSpec = interfaceSpecList.findInterfaceInfo(sndCode, rcvCode, transactionCode.getCode());
		String resInterfaceId = interfaceSpec.getInterfaceId();
		String resEsbTxId = replaceTxId(apiMessage.getTransactionId(), resInterfaceId);
		apiMessage.setInterfaceId(resInterfaceId);
		apiMessage.setTransactionId(resEsbTxId);
		apiMessage.setStatusCode(TcpStatusCode.SUCCESS.getCode());
		apiMessage.setEsbErrorMessage(TcpStatusCode.SUCCESS.getDescription());
	}

	private String replaceTxId(String originalTxId, String resInterfaceId) {
		String nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

		int lastUnderscoreIndex = originalTxId.lastIndexOf('_');
		if (lastUnderscoreIndex == -1) {
			throw new IllegalArgumentException("잘못된 트랜잭션 ID 형식입니다.");
		}

		String suffix = originalTxId.substring(lastUnderscoreIndex);

		return resInterfaceId + "_" + nowDateTime + suffix;
	}
}
