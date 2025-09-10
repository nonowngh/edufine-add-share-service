package mb.fw.policeminwon.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "tcp.client.async-connction", ignoreUnknownFields = true)
public class AsyncConnectionProperties {

	private List<AsyncConnection> connections;

	@Data
	public static class AsyncConnection {
		private String systemCode;
		private String host;
		private int port;
		private int reconnectDelaySec = 30;
	}
}
