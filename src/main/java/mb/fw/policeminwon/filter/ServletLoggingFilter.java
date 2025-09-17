package mb.fw.policeminwon.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.jms.core.JmsTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import mb.fw.atb.util.ATBUtil;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.constants.TcpStatusCode;
import mb.fw.policeminwon.filter.wrapper.CachedBodyHttpServletRequest;
import mb.fw.policeminwon.spec.InterfaceSpec;
import mb.fw.policeminwon.spec.InterfaceSpecList;
import mb.fw.policeminwon.web.dto.ESBApiMessage;

@Slf4j
@WebFilter("/esb/api/proxy/*")
public class ServletLoggingFilter implements Filter {

	private InterfaceSpecList interfaceSpecList;
	private JmsTemplate jmsTemplate;

	public ServletLoggingFilter(InterfaceSpecList interfaceSpecList, JmsTemplate jmsTemplate) {
		this.interfaceSpecList = interfaceSpecList;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		log.info("Proxy servlet filter in...");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

		// ESBApiRequest로 deserialize
		ESBApiMessage apiRequest = new ObjectMapper().readValue(cachedRequest.getReader(), ESBApiMessage.class);
		String interfaceId = apiRequest.getInterfaceId();
		String esbTxId = apiRequest.getTransactionId();
		if (interfaceId == null) {
			throw new IllegalArgumentException("interfaceId는 null일 수 없습니다.");
		}

		log.info("===[{}] 처리 시작===", esbTxId);
		// InterfaceInfo
		InterfaceSpec interfaceSpec = interfaceSpecList.findInterfaceInfo(interfaceId);
		String from = interfaceSpec.getSndCode();
		String to = interfaceSpec.getRcvCode();
		String description = interfaceSpec.getDescription();

		logTransaction(description, from, to, esbTxId);

		if (jmsTemplate != null && interfaceSpec.isLogging()) {
			String nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
			try {
				ATBUtil.startLogging(jmsTemplate, interfaceSpec.getInterfaceId(), esbTxId, null, 1,
						interfaceSpec.getSndCode(), interfaceSpec.getRcvCode(), nowDateTime, null);
			} catch (Exception e) {
				log.error("JMS start logging error!!!", e);
			}
		}
		String statusCd = TcpStatusCode.SUCCESS.getCode().equals(apiRequest.getStatusCode()) ? "S" : "F";
		String errorMsg = apiRequest.getEsbErrorMessage();
		int errorCnt = statusCd == "S" ? 0 : 1;
		// 필터 후 처리를 위해 chain.doFilter 호출
		try {
			chain.doFilter(cachedRequest, response);
		} catch (Exception e) {
			log.error("RestController 처리 중 오류!", e);
			statusCd = "F";
			errorMsg = e.getMessage();
			errorCnt = 1;
		} finally {
			try {
				if (jmsTemplate != null && interfaceSpec.isLogging()) {
					ATBUtil.endLogging(jmsTemplate, interfaceSpec.getInterfaceId(), esbTxId, "", errorCnt, statusCd,
							errorMsg, null);
				}
			} catch (Exception e) {
				log.error("JMS end logging error!!!", e);
			}
			log.info("===[{}] 처리 종료===", esbTxId);
		}
	}

	private static void logTransaction(String description, String from, String to, String esbTxId) {
		if (SystemCodeConstatns.SUMMRAY.equals(to)) {
			log.info("{} - api call...[{}] -> [{}] | esb-tran-id({})", description, from, to, esbTxId);
		} else if (SystemCodeConstatns.SUMMRAY.equals(from)) {
			log.info("{} - tcp send...[{}] -> [{}] | esb-tran-id({})", description, from, to, esbTxId);
		} else {
			log.info("{} - bypass...[{}] -> [{}] | esb-tran-id({})", description, from, to, esbTxId);
		}
	}

}
