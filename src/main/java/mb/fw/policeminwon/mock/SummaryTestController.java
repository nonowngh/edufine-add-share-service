package mb.fw.policeminwon.mock;

import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.utils.ByteBufUtils;

@Profile("mock-server")
@Slf4j
@RestController
@RequestMapping("/sjand/cmn")
public class SummaryTestController {

	@Value("${mock.response.success-view-billing-detail:true}")
	private boolean mockViewBillingDetail;
	@Value("${mock.response.success-payment-result-notification:false}")
	private boolean mockPaymentResultNotification;
	@Value("${mock.response.success-cancel-payment:true}")
	private boolean mockCancelPayment;

	@PostMapping(value = "/getInfrmDsctnDtlInq.do", produces = "application/json; charset=UTF-8")
	public String simulateViewBillngDetail(@RequestBody Map<String, Object> requestBody) {
		// 요청 로깅
		log.info("Received request: " + requestBody);
		if (mockViewBillingDetail)
			return "{\"inputData\":{\"eltrPymNo\":\"0137202530000015506\",\"randNo\":\"20251117131120047828109351838060\",\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\"},\"outputData\":{\"eltrPymNo\":\"0137202530000015506\",\"randNo\":\"20251117131120047828109351838060\",\"userType\":0,\"userRrno\":\"7702221392915\",\"userBznNo\":\"\",\"userFulnm\":\"신중구\",\"userRrno2\":\"7702221392915\",\"wrintNo\":\"200330000015506\",\"fineType\":3,\"jrdtPlstNm\":\"서울서대문경찰서\",\"pcptaxColctrAcno\":\"014135\",\"pcptaxColctrSmallAcnutCd\":\"0\",\"btddtAmt\":75000,\"aftdteAmt\":0,\"incmeMokCd\":\"561\",\"accnutYr\":\"2025\",\"btddtPymTmlmtDt\":\"20160120\",\"aftdtePymYmlmtDt\":\"\",\"fineCauseDate\":\"\",\"violDate\":\"\",\"violPlace\":\"\",\"violDtl\":\"\",\"violCarNo\":\"\",\"lawNm\":\"경범죄 처벌법 제8조, 제8조의2\",\"payDt\":\"\",\"dedtClCd\":\"B\",\"payerRealFulnm\":\"신중구\",\"cardPymPsbyYn\":\"Y\"},\"resMsg\":\"정상\",\"resCode\":\"000\"}";
		else
			return "{\"inputData\":{\"eltrPymNo\":\"0137202530000015506\",\"randNo\":\"20251117131120047828109351838060\",\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\"},\"resMsg\":\"난수오류\",\"resCode\":\"119\"}";
	}

	@PostMapping(value = "/savePayRsltAvtsmt.do", produces = "application/json; charset=UTF-8")
	public String simulatePaymentResultNotification(@RequestBody Map<String, Object> requestBody) {
		// 요청 로깅
		log.info("Received request: " + requestBody);
		if (mockPaymentResultNotification)
			return "{\"inputData\":{\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\",\"payerRrno\":\"6204211654712\",\"pcptaxColctrAcno\":\"017598\",\"eltrPymNo\":\"0137202530000015506\",\"payAmt\":\"75000\",\"payYmd\":\"20180713\",\"roffFncInstCd\":\"3650000\",\"realPayerRrno\":\"6204211654712\",\"paySysCd\":\"V\",\"payCd\":\"Q\"},\"outputData\":{\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\",\"payerRrno\":\"6204211654712\",\"pcptaxColctrAcno\":\"017598\",\"eltrPymNo\":\"0137202530000015506\",\"payAmt\":\"75000\",\"payYmd\":\"20180713\",\"roffFncInstCd\":\"3650000\",\"realPayerRrno\":\"6204211654712\",\"paySysCd\":\"V\",\"payCd\":\"Q\",\"apaySysCd\":\"\"},\"resMsg\":\"정상\",\"resCode\":\"000\"}";
		else
			return "{\"inputData\":{\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\",\"payerRrno\":\"6204211654712\",\"pcptaxColctrAcno\":\"017598\",\"eltrPymNo\":\"0137202530000015506\",\"payAmt\":\"75000\",\"payYmd\":\"20180713\",\"roffFncInstCd\":\"3650000\",\"realPayerRrno\":\"6204211654712\",\"paySysCd\":\"V\",\"payCd\":\"Q\"},\"resMsg\":\"기 납부내역(납부 불가)\",\"resCode\":\"331\"}";
	}

	@PostMapping(value = "/savePayRtrcn.do", produces = "application/json; charset=UTF-8")
	public String simulateCancelPayment(@RequestBody Map<String, Object> requestBody) {
		// 요청 로깅
		log.info("Received request: " + requestBody);
		if (mockCancelPayment)
			return "{\"inputData\":{\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\",\"roffFncInstCd\":\"3650000\",\"realPayerRrno\":\"6204211654712\",\"orgdlTrsmDt\":\"250402004641\",\"orgdlPayAmt\":\"32000\",\"rtrcnRsn\":\"S\",\"orgdlPayCd\":\"Q\"},\"outputData\":{\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\",\"roffFncInstCd\":\"3650000\",\"realPayerRrno\":\"6204211654712\",\"orgdlTrsmDt\":\"250402004641\",\"orgdlPayAmt\":\"32000\",\"rtrcnRsn\":\"S\",\"orgdlPayCd\":\"Q\"},\"resMsg\":\"정상\",\"resCode\":\"000\"}";
		else
			return "{\"inputData\":{\"centTranNo\":\"CT000957020\",\"orgaTranNo\":\"PL097539381\",\"roffFncInstCd\":\"3650000\",\"realPayerRrno\":\"6204211654712\",\"orgdlTrsmDt\":\"250402004641\",\"orgdlPayAmt\":\"32000\",\"rtrcnRsn\":\"S\",\"orgdlPayCd\":\"Q\"},\"resMsg\":\"기 취소된 거래임\",\"resCode\":\"413\"}";
	}

	public static void main(String[] args) {
		Charset charset = Charset.forName("MS949"); // TcpCommonSettingConstants.MESSAGE_CHARSET 사용 가능
		String message = "IGN0990800121002   C   2025072909400CT123465678            121234567  36500006204211654712CT000957020 250402004641                000000000032000SQ         ";
		// String → ByteBuf
		ByteBuf buf = Unpooled.copiedBuffer(message, charset);
		// ByteBuf 읽기
		System.out.println(ByteBufUtils.addMessageLength(buf).toString(charset));
//        String readMessage = buf.toString(charset);
		buf.release();

	}
}
