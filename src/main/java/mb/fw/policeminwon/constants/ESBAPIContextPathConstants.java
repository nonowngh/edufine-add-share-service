package mb.fw.policeminwon.constants;

public class ESBAPIContextPathConstants {

	private ESBAPIContextPathConstants() {
	}

	// 경찰청 범칙금 - 과태료 고지내역 상세 조회
	public static final String VIEW_VIEW_BILLING_DETAIL = "/view-billing-detail";
	// 경찰청 범칙금 - 과태료 납부결과 통지
	public static final String PAYMENT_RESULT_NOTIFICATION = "/payment-result-notification";
	// 경찰청 범칙금 - 과태료 납부 (재)취소
	public static final String CANCEL_PAYMENT = "/cancel-payment";

	// 에러 처리
	public static final String ERROR = "/error";
}
