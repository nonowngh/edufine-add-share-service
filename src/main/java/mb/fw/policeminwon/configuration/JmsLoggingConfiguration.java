package mb.fw.policeminwon.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import com.indigo.indigomq.pool.PooledConnectionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "jms.logging.enabled", havingValue = "true", matchIfMissing = true)
public class JmsLoggingConfiguration {

	@Bean("esbJmsTemplate")
	JmsTemplate jmsTemplate(@Autowired(required = false) PooledConnectionFactory jmsConnectionFactory) {
		if (jmsConnectionFactory != null) {
			log.info("Loading esb jmsTemplate bean -> " + jmsConnectionFactory.getConnectionFactory());
			return new JmsTemplate(jmsConnectionFactory);
		}
		return null;
	}
}
