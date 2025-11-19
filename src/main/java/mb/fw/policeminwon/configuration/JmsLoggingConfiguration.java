package mb.fw.policeminwon.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import com.indigo.indigomq.pool.PooledConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "jms.logging.enabled", havingValue = "true")
public class JmsLoggingConfiguration {

	@Bean("esbJmsTemplate")
	JmsTemplate jmsTemplate(@Autowired(required = false) PooledConnectionFactory jmsConnectionFactory) {
		if (jmsConnectionFactory != null)
			return new JmsTemplate(jmsConnectionFactory);
		return null;
	}
}
