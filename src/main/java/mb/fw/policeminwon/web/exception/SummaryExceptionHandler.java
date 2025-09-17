package mb.fw.policeminwon.web.exception;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.web.dto.ESBApiMessage;

@Slf4j
@RestControllerAdvice
public class SummaryExceptionHandler {
	
	@Autowired
	WebClient callBackWebClient;

	@ExceptionHandler(CustomException.class)
	public void handleCustomException(CustomException ex) {
		log.error("CustomException -> " + ex.getMessage());
		ESBApiMessage apiMessage = ex.getApiMessage();
		apiMessage.setStatusCode(ex.getStatusCode().getCode());
		apiMessage.setEsbErrorMessage("["+ex.getStatusCode().getDescription() + "]" + ex.getMessage());
		CompletableFuture.runAsync(() -> {
			callBackWebClient.post().uri(ESBAPIContextPathConstants.ERROR).contentType(MediaType.APPLICATION_JSON).bodyValue(apiMessage)
					.retrieve().toBodilessEntity().doOnSuccess(r -> log.info("Callback success"))
					.doOnError(e -> log.error("Callback failed", e)).subscribe();
		});
	}
}
