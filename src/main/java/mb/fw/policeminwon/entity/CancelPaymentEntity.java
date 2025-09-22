package mb.fw.policeminwon.entity;

import lombok.Data;

@Data
public class CancelPaymentEntity {
	
	private Integer bankBranchCode;      // 출금 금융회사 점별 코드 (N, 7)
    private String payerRegNo;          // 납부자 주민(사업자) 등록번호 (AN, 13)
    private String originCenterMsgNo;   // 원거래 센터 전문 관리 번호 (AN, 12)
    private String originSendDateTime;  // 원거래 전송 일시 (N, 12)
    private String reserveField1;       // 예비 정보 FIELD 1 (AN, 16)
    private Integer originPayAmount;     // 원거래 납부 금액 (N, 15)
    private String cancelReason;        // 취소 사유 (AN, 1)
    private String originPayType;       // 원거래 납부 형태 구분 (AN, 1)
    private String reserveField2;       // 예비 정보 FIELD 2 (AN, 9)

}
