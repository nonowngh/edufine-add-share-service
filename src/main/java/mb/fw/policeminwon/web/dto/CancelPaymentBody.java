package mb.fw.policeminwon.web.dto;

import lombok.Data;

@Data
public class CancelPaymentBody {
	// 센터전문관리번호
	private String centTranNo;
	// 이용기관전문관리번호
	private String orgaTranNo;
	// 출금 금융회사 점별 코드
	private String roffFncInstCd;
	// 납부자 주민(사업자)등록번호
	private String realPayerRrno;
	// 원거래 전송 일시
	private String orgdlTrsmDt;
	// 원거래 납부 금액
	private String orgdlPayAmt;
	// 취소사유
	private String rtrcnRsn;
	// 원거래 납부 형태 구분
	private String orgdlPayCd;
}
