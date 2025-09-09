package mb.fw.policeminwon.utils;

import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.constants.TcpHeaderSrFlag;
import mb.fw.policeminwon.constants.TcpHeaderTransactionCode;

@Slf4j
public class LogUtils {

	public static void loggingLouteInfo(String transactionCode, String srFlag, boolean isSummray) {
		String targetSystemCode = TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstatns.KFTC
				: SystemCodeConstatns.TRAFFIC;
		String from = targetSystemCode.equals(SystemCodeConstatns.KFTC) ? "교통(TCS)" : "금결원(KFT)";
		String to = targetSystemCode.equals(SystemCodeConstatns.KFTC) ? "금결원(KFT)" : "교통(TCS)";
		String reqOrRes = targetSystemCode.equals(SystemCodeConstatns.KFTC) ? "응답" : "요청";
		if (isSummray) {
			from = targetSystemCode.equals(SystemCodeConstatns.KFTC) ? "즉심(SJS)" : "금결원(KFT)";
			to = targetSystemCode.equals(SystemCodeConstatns.KFTC) ? "금결원(KFT)" : "즉심(SJS)";
			targetSystemCode = TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag) ? SystemCodeConstatns.KFTC
					: SystemCodeConstatns.SUMMRAY;
		}
		switch (transactionCode) {
		case TcpHeaderTransactionCode.TEST_CALL:
			log.info("테스트 콜 {}...[{}] -> [{}]", reqOrRes, from, to);
			break;
		case TcpHeaderTransactionCode.VIEW_BILLING_DETAIL:
			logTransaction(targetSystemCode, reqOrRes, from, to, isSummray, "고지내역 상세조회");
			break;
		case TcpHeaderTransactionCode.PAYMENT_RESULT_NOTIFICATION:
			if(TcpHeaderSrFlag.KFTC.equalsIgnoreCase(srFlag)) to = "교통(TCS), 즉심(SJS)";
			logTransaction(targetSystemCode, reqOrRes, from, to, isSummray, "납부결과 통지");
			break;
		case TcpHeaderTransactionCode.CANCEL_PAYMENT:
			logTransaction(targetSystemCode, reqOrRes, from, to, isSummray, "납부 (재)취소");
			break;
		default:
			log.error("알 수 없는 거래 구분 코드 : {}", transactionCode);
			break;
		}
	}

	private static void logTransaction(String targetSystemCode, String reqOrRes, String from, String to,
			boolean isSummray, String transactionType) {
		if (SystemCodeConstatns.SUMMRAY.equals(targetSystemCode)) {
			log.info("{} {} - api call...[{}] -> [{}]", transactionType, reqOrRes, from, to);
		} else if (isSummray) {
			log.info("{} {} - tcp send...[{}] -> [{}]", transactionType, reqOrRes, from, to);
		} else {
			log.info("{} {} - bypass...[{}] -> [{}]", transactionType, reqOrRes, from, to);
		}
	}
}
