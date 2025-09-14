package mb.fw.policeminwon.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ESBAPIContextPathConstants;
import mb.fw.policeminwon.constants.SystemCodeConstatns;

@Slf4j
@WebFilter("/*")
public class ServletLoggingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		// ContextPath 가져오기
		String contextPath = httpRequest.getRequestURI();

		if (contextPath.endsWith(ESBAPIContextPathConstants.VIEW_VIEW_BILLING_DETAIL)) {
			logTransaction(SystemCodeConstatns.KFTC, "응답", "즉심(SJS)", "금결원(KFT)", true, "고지내역 상세조회");
		} else if (contextPath.endsWith(ESBAPIContextPathConstants.PAYMENT_RESULT_NOTIFICATION)) {
			logTransaction(SystemCodeConstatns.KFTC, "응답", "즉심(SJS)", "금결원(KFT)", true, "납부결과 통지");
		} else if (contextPath.endsWith(ESBAPIContextPathConstants.CANCEL_PAYMENT)) {
			logTransaction(SystemCodeConstatns.KFTC, "응답", "즉심(SJS)", "금결원(KFT)", true, "납부 (재)취소");
		} else {
			log.error("Invalid ContextPath = '{}'", contextPath);
		}
		// 필터 후 처리를 위해 chain.doFilter 호출
		chain.doFilter(request, response);
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
