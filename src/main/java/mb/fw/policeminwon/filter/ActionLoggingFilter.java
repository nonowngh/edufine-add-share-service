package mb.fw.policeminwon.filter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.jms.core.JmsTemplate;

import lombok.extern.slf4j.Slf4j;
import mb.fw.atb.util.ATBUtil;
import mb.fw.atb.util.TransactionIdGenerator;
import mb.fw.policeminwon.constants.SystemCodeConstatns;
import mb.fw.policeminwon.spec.InterfaceInfo;

@Slf4j
public class ActionLoggingFilter {

	public static Runnable routeLoggingFilter(Runnable action, InterfaceInfo interfaceInfo, JmsTemplate jmsTemplate) {
		return () -> {
			String nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
				
			// ESB 트랜젝션 아이디 생성
			String esbTxId = TransactionIdGenerator.generate(interfaceInfo.getInterfaceId(), "", nowDateTime);
			// 첫번째 로깅
			if (jmsTemplate != null && interfaceInfo.isLogging()) {
				try {
					ATBUtil.startLogging(jmsTemplate, interfaceInfo.getInterfaceId(), esbTxId, null, 1,
							interfaceInfo.getSndCode(), interfaceInfo.getRcvCode(), nowDateTime, null);
				} catch (Exception e) {
					log.error("JMS start logging error!!!", e);
				}
			}

			String from = interfaceInfo.getSndCode();
			String to = interfaceInfo.getRcvCode();
			String description = interfaceInfo.getDescription();

			logTransaction(description, from, to, esbTxId);

			String statusCd = "S";
			String errorMsg = "SUCCESS";
			int errorCnt = 0;
			try {
				action.run();
			} catch (Exception e) {
				statusCd = "F";
				errorMsg = e.getMessage();
				errorCnt = 1;
				log.error("Error occurred during action execution: " + errorMsg);
			} finally {
				// 두번째 로깅
				if (jmsTemplate != null && interfaceInfo.isLogging()) {
					try {
						ATBUtil.endLogging(jmsTemplate, interfaceInfo.getInterfaceId(), esbTxId, null, errorCnt,
								statusCd, errorMsg, null);
					} catch (Exception e) {
						log.error("JMS end logging error!!!", e);
					}
				}
			}
		};
	}

	private static void logTransaction(String description, String from, String to, String esbTxId) {
		if (SystemCodeConstatns.SUMMRAY.equals(to)) {
			log.info("{} - api call...[{}] -> [{}] | tran-id({})", description, from, to, esbTxId);
		} else if (SystemCodeConstatns.SUMMRAY.equals(from)) {
			log.info("{} - tcp send...[{}] -> [{}] | tran-id({})", description, from, to, esbTxId);
		} else {
			log.info("{} - bypass...[{}] -> [{}] | tran-id({})", description, from, to, esbTxId);
		}
	}
}
