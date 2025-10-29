package mb.fw.policeminwon.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import com.indigo.indigomq.pool.PooledConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "jms.logging.enabled", havingValue = "true")
public class JmsLoggingConfiguration {

	@Autowired(required = false)
	private PooledConnectionFactory pooledConnectionFactory;

	@Bean(name = "esbJmsTemplate")
	JmsTemplate jmsTemplate() {
	    return Optional.ofNullable(pooledConnectionFactory)
                .map(JmsTemplate::new)
                .orElse(null);
	}
}
