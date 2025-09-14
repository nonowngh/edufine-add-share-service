package mb.fw.policeminwon.spec;

import lombok.Data;

@Data
public class InterfaceInfo {

	// 인터페이스 아이디
	private String interfaceId;
	// 전문 거래구분 코드
	private String messageCode;
	// 송신 시스템 코드
	private String sndCode;
	// 수신 시스템 코드
	private String rcvCode;
	// 인터페이스 설명
	private String description;

}
