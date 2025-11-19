package mb.fw.policeminwon.web.dto;

import lombok.Data;

@Data
public class PaymentResultNotificationBody {
	// 센터전문관리번호
	private String centTranNo;
	// 이용기관전문관리번호
	private String orgaTranNo;
	// 납부의무자 주민(사업자,법인)등록번호
	private String payerRrno;
	// 징수관 계좌번호
	private String pcptaxColctrAcno;
	// 전자납부번호
	private String eltrPymNo;
	// 납부금액
	private String payAmt;
	// 납부일자
	private String payYmd;
	// 출금 금융회사 점별 코드
	private String roffFncInstCd;
	// 납부자 주민(사업자)등록번호
	private String realPayerRrno;
	// 납부이용시스템
	private String paySysCd;
	// 납부형태구분
	private String payCd;
	// 기납부이용시스템
	private String apaySysCd;
}
