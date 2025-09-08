package mb.fw.policeminwon.web.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import mb.fw.policeminwon.entity.ViewBillingDetailEntity;
import mb.fw.policeminwon.parser.ViewBillingDetailParser;
import mb.fw.policeminwon.web.dto.ESBApiRequest;
import mb.fw.policeminwon.web.mapper.ViewBillingDetailMapper;

@lombok.extern.slf4j.Slf4j
@Service
public class SummaryService {

	private final WebClient callBackWebClient;

	private final ViewBillingDetailMapper viewBillingDetailMapper;

	public SummaryService(ViewBillingDetailMapper viewBillingDetailMapper,
			@Qualifier("callBackWebClient") WebClient callBackWebClient) {
		this.viewBillingDetailMapper = viewBillingDetailMapper;
		this.callBackWebClient = callBackWebClient;
	}

	public void doAsyncProcess(ESBApiRequest request) {
		String elecPayNo =  ViewBillingDetailParser.toEntity(request.getBodyData()).getElecPayNo();
		ViewBillingDetailEntity entity = viewBillingDetailMapper.selectBillingDetailByElecPayNo(elecPayNo);
		if(entity == null) {
			log.error("elecPayNo '{}' 해당하는 정보가 없음", elecPayNo);
			return;
		}
		String returnMessage = ViewBillingDetailParser.toMessage(entity);

		CompletableFuture.runAsync(() -> {
			request.setBodyData(returnMessage);

			callBackWebClient.post().contentType(MediaType.APPLICATION_JSON).bodyValue(request).retrieve()
					.toBodilessEntity().doOnSuccess(r -> log.info("Callback success"))
					.doOnError(e -> log.error("Callback failed", e)).subscribe();
		});
	}

}
