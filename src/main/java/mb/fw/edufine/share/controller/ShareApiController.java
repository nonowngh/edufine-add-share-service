package mb.fw.edufine.share.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

	public static void main(String[] args) {
		Map<String, Object> root = new HashMap<>();
		List<Map<String, Object>> dataList = new ArrayList<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("CVA_KND_CD", "B0007");
		paramMap.put("TXPR_CL_CD", "01");
		paramMap.put("TXPR_NM", "홍길동");
		paramMap.put("RESNO", "555");
		paramMap.put("BSNO", "333");
		paramMap.put("CPRNO", "123");
		paramMap.put("TELNO", "");
		paramMap.put("MPNO", "");
		paramMap.put("EML", "");
		paramMap.put("ROAD_NM_ADR", "");
		paramMap.put("LD_ADR", "");
		paramMap.put("RESNO_OP_YN", "");
		paramMap.put("ADR_OP_YN", "");
		paramMap.put("CERP_ISN_RQS_QTY", "");
		paramMap.put("CVA_DCUM_GRAN_MTHD_CD", "");
		paramMap.put("CVA_DCUM_SBMS_ORGN_CL_CD", "");
		paramMap.put("PLSB_NM", "");
		paramMap.put("NNF_CL_CD", "");
		paramMap.put("PSP_RGT_NO", "");
		paramMap.put("PSP_FNM", "홍길동");
		paramMap.put("PSP_RESNO", "11111111111111");
		paramMap.put("PSP_CRTF", "2222222222");
		paramMap.put("PRIC_PYMN_AMT", "1023");
		paramMap.put("PRIC_APE_DT", "333");
		paramMap.put("PRIC_PYMN_CHRG_FNM", "");
		paramMap.put("PRIC_PYMN_TEL_NO", "000-111-2222");
		paramMap.put("RCAT_MTHD_CD", "23");
		paramMap.put("ISN_APLN_ORGN_CD", "1741000");
		paramMap.put("ISN_APLN_ORGN_TELNO", "");
		dataList.add(paramMap);
		root.put("data", dataList);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String bodyStr = gson.toJson(root);

		System.out.println(bodyStr);
		// 1. bodyStr 암호화, 전자서명, 인코딩 처리
		
		// 2. http 헤더 구성(api key 추가)
		
		// 3. 행공 call
		
		// 4. 행공 응답 str 디코딩, 서명체크, 복호화
		
		// 5. str -> object
		Map<String, Object> bodyMap = gson.fromJson(bodyStr, Map.class);
		ArrayList<Map<String, Object>> resultList = (ArrayList) bodyMap.get("data");
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("esb_data", resultList);
		System.out.println(gson.toJson(resultMap));
		
		// 6. esb_data 에 담아서 응답
		
		
	}

}
