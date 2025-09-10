package mb.fw.policeminwon.configuration;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;

@Data
@Configuration
@ConditionalOnProperty(name = "tcp.client.async-connction.enabled", havingValue = "true")
public class AsyncConnectionConfiguration {

	private final AsyncConnectionProperties asyncConnectionProperties;

	public AsyncConnectionConfiguration(AsyncConnectionProperties asyncConnectionProperties) {
		this.asyncConnectionProperties = asyncConnectionProperties;
	}

	@Bean
	List<AsyncConnectionClient> clients() {
		return asyncConnectionProperties.getConnections().stream().map(asyncConnection -> {
			AsyncConnectionClient client = new AsyncConnectionClient(asyncConnection.getSystemCode(),
					asyncConnection.getHost(), asyncConnection.getPort(), asyncConnection.getReconnectDelaySec());
			client.start();
			return client;
		}).collect(Collectors.toList());
	}

//	@Bean(initMethod = "start", destroyMethod = "shutdown")
//	AsyncConnectionClient client() {
//		return new AsyncConnectionClient(host, port, reconnectDelaySec);
//	}
}
