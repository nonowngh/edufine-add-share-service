package mb.fw.policeminwon.web.dto;

import lombok.Data;

@Data
public class ViewBillingDetailBody {
	// 전자납부번호
	private String eltrPymNo;
	// 난수
	private String randNo;
	// 센터전문관리번호
	private String centTranNo;
	// 이용기관전문관리번호
	private String orgaTranNo;
	// (회원정보연계)회원 유형(0:개인, 1:사업자-개인, 2:법인 9:비회원)
	private String userType;
	// (회원정보연계)회원 주민등록번호
	private String userRrno;
	// (회원정보연계)회원 사업자등록번호
	private String userBznNo;
	// (회원정보연계)회원명
	private String userFulnm;
	// 납부의무자 주민(사업자,법인)등록번호
	private String userRrno2;
	// 납부자(고지서)번호
	private String wrintNo;
	// 과금 종류('3' : 경범죄 범칙금)
	private String fineType;
	// 징수 기관명
	private String jrdtPlstNm;
	// 징수관 계좌번호
	private String pcptaxColctrAcno;
	// 소계정
	private String pcptaxColctrSmallAcnutCd;
	// 납기내 금액
	private String btddtAmt;
	// 납기후 금액
	private String aftdteAmt;
	// 징수 과목 코드(세목 코드)
	private String incmeMokCd;
	// 징수 결의 회계 년도
	private String accnutYr;
	// 납기일(납기내)
	private String btddtPymTmlmtDt;
	// 납기일(납기후)
	private String aftdtePymYmlmtDt;
	// 과세 원인 일시(선택)
	private String fineCauseDate;
	// 위반 일시(선택)
	private String violDate;
	// 위반 장소(선택)
	private String violPlace;
	// 위반 내용(선택)
	private String violDtl;
	// 위반 차량 번호(선택)
	private String violCarNo;
	// 법령 근거 (선택)
	private String lawNm;
	// 납부 일시(선택)
	private String payDt;
	// 납기 내후 구분
	private String dedtClCd;
	// 납부의무자 성명
	private String payerRealFulnm;
	// 신용카드 납부 제한 여부
	private String cardPymPsbyYn;
}
