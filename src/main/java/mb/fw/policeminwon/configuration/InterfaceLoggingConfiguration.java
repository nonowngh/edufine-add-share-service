package mb.fw.policeminwon.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import com.indigo.indigomq.pool.PooledConnectionFactory;

@Configuration
public class InterfaceLoggingConfiguration {

	@Autowired(required = false)
	private PooledConnectionFactory pooledConnectionFactory;

	@Bean
	JmsTemplate jmsTemplate() {
	    return Optional.ofNullable(pooledConnectionFactory)
                .map(JmsTemplate::new)
                .orElse(null);
	}
}
