package mb.fw.policeminwon.entity;

import lombok.Data;

/**
 * 경찰청 범칙금 - TestCall
 */
@Data
public class CommonHeaderEntity {
	private String msgLength;           // 전문 길이(4)
    private String jobType;             // 업무 구분(3)
    private String orgCode;             // 기관 코드(3)
    private String msgType;             // 전문 종별 코드(4)
    private String trCode;              // 거래 구분 코드(6)
    private String statusCode;          // 상태 코드(3)
    private String flag;                // 송수신 FLAG(1)
    private String respCode;            // 응답 코드(3)
    private String sendTime;            // 전송 일시(12)
    private String centerMsgNo;         // 센터 전문 관리 번호(12)
    private String orgMsgNo;            // 이용기관 전문 관리 번호(12)
    private String orgTypeCode;         // 이용기관 발행기관 분류코드(2)
    private String orgGiroNo;           // 이용기관 지로번호(7)
    private String filler;              // 여분 필드(2)
    
}
