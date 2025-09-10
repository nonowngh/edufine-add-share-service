package mb.fw.policeminwon.entity;

import lombok.Data;

/**
 * 경찰청 범칙금 - 과태료 납부결과 통지
 */
@Data
public class PaymentResultNotificationEntity {
	private String obligorRegNo; // 납부의무자 주민(사업자, 법인) 등록번호 (AN, 13)
	private String collectorAccountNo; // 징수관 계좌번호 (AN, 6)
	private String elecPayNo; // 전자납부번호 (AN, 19)
	private String reserveField1; // 예비정보 FIELD 1 (AN, 3)
	private String reserveField2; // 예비정보 FIELD 2 (AN, 7)
	private Integer payAmount; // 납부 금액 (N, 15)
	private String payDate; // 납부 일자 (N, 8)
	private Integer bankBranchCode; // 출금 금융회사 점별 코드 (N, 7)
	private String reserveField3; // 예비정보 FIELD 3 (AN, 16)
	private String reserveField4; // 예비정보 FIELD 4 (ANS, 14)
	private String payerRegNo; // 납부자 주민(사업자) 등록 번호 (AN, 13)
	private String reserveField5; // 예비정보 FIELD 5 (AHNS, 10)
	private String reserveField6; // 예비정보 FIELD 6 (AHNS, 10)
	private String paySystem; // 납부 이용 시스템 (AN, 1)
	private String prePaySystem; // 기 납부 이용 시스템 (AN, 1)
	private String payType; // 납부 형태 구분 (AN, 1)
	private String reserveField7; // 예비정보 FIELD 7 (AN, 9)
}
