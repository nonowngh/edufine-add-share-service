package mb.fw.policeminwon.web.exception;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.parser.CommonHeaderParser;
import mb.fw.policeminwon.utils.ByteBufUtils;
import mb.fw.policeminwon.web.dto.ESBApiMessage;

@Slf4j
@RestControllerAdvice
public class SummaryExceptionHandler {

	@Autowired(required = false)
	WebClient callBackWebClient;

	@ExceptionHandler(CustomServletException.class)
	public void handleCustomServletException(CustomServletException ex) {
		log.error("CustomException -> " + ex.getMessage());
		ESBApiMessage apiMessage = ex.getApiMessage();
		apiMessage.setStatusCode(ex.getStatusCode().getCode());
		apiMessage.setEsbErrorMessage("[" + ex.getStatusCode().getDescription() + "]" + ex.getMessage());
		CompletableFuture.runAsync(() -> {
			callBackWebClient.post().uri(ESBAPIContextPathConstants.ERROR).contentType(MediaType.APPLICATION_JSON)
					.bodyValue(apiMessage).retrieve().toBodilessEntity().doOnSuccess(r -> log.info("Callback success"))
					.doOnError(e -> log.error("Callback failed", e)).subscribe();
		});
	}
}
