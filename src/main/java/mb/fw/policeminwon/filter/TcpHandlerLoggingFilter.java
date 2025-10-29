package mb.fw.policeminwon.filter;

import org.springframework.jms.core.JmsTemplate;

import lombok.extern.slf4j.Slf4j;
import mb.fw.atb.util.ATBUtil;
import mb.fw.policeminwon.spec.InterfaceSpec;
import mb.fw.policeminwon.utils.CommonLoggingUtils;
import reactor.core.publisher.Mono;

@Slf4j
public class TcpHandlerLoggingFilter {

	public static Mono<Void> routeLoggingFilter(Mono<Void> action, InterfaceSpec interfaceSpec,
			JmsTemplate jmsTemplate, String esbTxId, String nowDateTime, String responseCode) {

		if (jmsTemplate != null && interfaceSpec.isLogging()) {
			try {
				ATBUtil.startLogging(jmsTemplate, interfaceSpec.getInterfaceId(), esbTxId, null, 1,
						interfaceSpec.getSndCode(), interfaceSpec.getRcvCode(), nowDateTime, null);
			} catch (Exception e) {
				log.error("JMS start logging error!!!", e);
			}
		}

		String from = interfaceSpec.getSndCode();
		String to = interfaceSpec.getRcvCode();
		String description = interfaceSpec.getDescription();
		CommonLoggingUtils.logTransaction(description, from, to, esbTxId);

		return action.doOnSubscribe(res -> log.info("===[{}] 처리 시작===", esbTxId)).doOnSuccess(res -> {
			try {
				if (jmsTemplate != null && interfaceSpec.isLogging()) {
					ATBUtil.endLogging(jmsTemplate, interfaceSpec.getInterfaceId(), esbTxId, "", 0, "S", "SUCCESS",
							null);
				}
			} catch (Exception e) {
				log.error("JMS end logging error!!!", e);
			}
		}).doOnError(error -> {
			log.error("Error during proxy server handler action : {}\n", error.getMessage(), error);
			try {
				if (jmsTemplate != null && interfaceSpec.isLogging()) {
					ATBUtil.endLogging(jmsTemplate, interfaceSpec.getInterfaceId(), esbTxId, "", 1, "F",
							error.getMessage(), null);
				}
			} catch (Exception e) {
				log.error("JMS end logging error!!!", e);
			}
		}).onErrorResume(error -> Mono.empty()).doFinally(signalType -> {
			CommonLoggingUtils.loggingTcpResponse(responseCode);
			log.info("===[{}] 처리 종료===", esbTxId);
		});
	}

}
