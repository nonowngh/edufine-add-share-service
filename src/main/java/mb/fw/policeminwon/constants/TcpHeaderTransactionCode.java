package mb.fw.policeminwon.constants;

import java.util.Arrays;

import lombok.Getter;

public enum TcpHeaderTransactionCode {

	// 테스트 콜
	TEST_CALL("000301"),
	// 고지내역 상세조회
	VIEW_BILLING_DETAIL("121002"),
	// 납부결과 통지
	PAYMENT_RESULT_NOTIFICATION("122001"),
	// 납부 (재)취소
	CANCEL_PAYMENT("992001");

	@Getter
	private final String code;

	TcpHeaderTransactionCode(String code) {
		this.code = code;
	}

	public static TcpHeaderTransactionCode fromCode(String code) {
		return Arrays.stream(TcpHeaderTransactionCode.values()).filter(value -> value.getCode().equals(code))
				.findFirst().orElse(null);
	}
}