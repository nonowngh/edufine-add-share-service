package mb.fw.policeminwon.mock;

import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mb.fw.policeminwon.utils.ByteBufUtils;

@RestController
@RequestMapping("/sjand/cmn")
public class SummaryTestController {

	@PostMapping(value = "/getInfrmDsctnDtlInq.do", produces = "application/json; charset=UTF-8")
	public String simulate(@RequestBody Map<String, Object> requestBody) {
		// 요청 로깅
		System.out.println("Received request: " + requestBody);

		String responseJson = "{\r\n"
				+ "\"inputData\": {\r\n"
				+ "  \"centTranNo\": \"CT000957020\",  \r\n"
				+ "  \"orgaTranNo\": \"PL097539381\", \r\n"
				+ "  \"payerRrno\": \"6204211654712\", \r\n"
				+ "  \"pcptaxColctrAcno\": \"017598\", \r\n"
				+ "  \"eltrPymNo\": \"0137200330000015506\", \r\n"
				+ "  \"payAmt\": \"75000\", \r\n"
				+ "  \"payYmd\": \"20180713\", \r\n"
				+ "  \"roffFncInstCd\": \"3650000\", \r\n"
				+ "  \"realPayerRrno\": \"6204211654712\", \r\n"
				+ "  \"paySysCd\": \"V\", \r\n"
				+ "  \"payCd\": \"Q\", \r\n"
				+ "},\r\n"
				+ "\"outputData\": {\r\n"
				+ "  \"centTranNo\": \"CT000957020\",  \r\n"
				+ "  \"orgaTranNo\": \"PL097539381\", \r\n"
				+ "  \"payerRrno\": \"6204211654712\", \r\n"
				+ "  \"pcptaxColctrAcno\": \"017598\", \r\n"
				+ "  \"eltrPymNo\": \"0137200330000015506\", \r\n"
				+ "  \"payAmt\": \"75000\", \r\n"
				+ "  \"payYmd\": \"20180713\", \r\n"
				+ "  \"roffFncInstCd\": \"3650000\", \r\n"
				+ "  \"realPayerRrno\": \"6204211654712\", \r\n"
				+ "  \"paySysCd\": \"V\", \r\n"
				+ "  \"payCd\": \"Q\", \r\n"
				+ "  \"apaySysCd\": \"\" \r\n"
				+ "},\r\n"
				+ "\"resMsg\": \"정상\",\r\n"
				+ "\"resCode\": \"000\"\r\n"
				+ "}";

		return responseJson;
	}
	
	public static void main(String[] args) {
		Charset charset = Charset.forName("MS949"); // TcpCommonSettingConstants.MESSAGE_CHARSET 사용 가능
		String message = "IGN0990800121002   C   2025072909400CT123465678            121234567  0137200330000015506한국어도 잇네~~~    20251117131120047828109351838060";
        // String → ByteBuf
        ByteBuf buf = Unpooled.copiedBuffer(message, charset);
        // ByteBuf 읽기
        System.out.println(ByteBufUtils.addMessageLength(buf).toString(charset));
//        String readMessage = buf.toString(charset);
        buf.release();
		
	}
}
