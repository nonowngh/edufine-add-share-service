package mb.fw.edufine.share.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mb.fw.edufine.share.constants.ShareServiceCode;
import mb.fw.edufine.share.dto.EsbRequestMessage;
import mb.fw.edufine.share.dto.EsbResponseMessage;
import mb.fw.edufine.share.service.ShareApiService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/next-share")
public class ShareApiController {

	private final ShareApiService service;

	public ShareApiController(ShareApiService service) {
		this.service = service;
	}

	@PostMapping("/call")
	public Mono<EsbResponseMessage> callNextShare(@RequestBody EsbRequestMessage request) {

		String targetSystemCode = request.getEsb_trg_sys();
		Map<ShareServiceCode, Mono<Void>> actions = new HashMap<>();
		// 국세청 국세납세증명
		actions.put(ShareServiceCode.NTS_NTP, service.callNltTaxpayProofService());
		// 국세청 전자세금계산서
		actions.put(ShareServiceCode.NTS_ETB, service.callElctrnTaxBillService());
		// 행정안정부 지방세납세증명
		actions.put(ShareServiceCode.MOS_LTP, service.callLlxTaxpayProofService());

		Mono<Void> action = actions.getOrDefault(ShareServiceCode.fromSystemCode(targetSystemCode),
				Mono.error(new IllegalArgumentException("Invalid transaction-code -> " + targetSystemCode)));
		action.doFinally(signal -> {
		}).subscribe();
		return null;

	}
}
