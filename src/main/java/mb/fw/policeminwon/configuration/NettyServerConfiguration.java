package mb.fw.policeminwon.configuration;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;
import mb.fw.policeminwon.netty.proxy.ProxyServer;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.spec.InterfaceInfoList;

@Data
@Configuration
@ConfigurationProperties(prefix = "tcp.server")
public class NettyServerConfiguration {

	private Proxy proxy;
	private Summary summary;

	@Data
	public static class Proxy {
		private int bindPort;
	}

	@Data
	public static class Summary {
		private int bindPort;
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	@ConditionalOnProperty(prefix = "tcp.server.proxy", name = "enabled", havingValue = "true")
	ProxyServer proxyServer(List<AsyncConnectionClient> clients, @Qualifier("webClient") Optional<WebClient> optionalWebClient, InterfaceInfoList interfaceInfoList, Optional<JmsTemplate> optionalJmsTemplate) {
		return new ProxyServer(proxy.getBindPort(), clients, optionalWebClient, interfaceInfoList, optionalJmsTemplate);
	}

//	@Bean(initMethod = "start", destroyMethod = "shutdown")
//	@ConditionalOnProperty(prefix = "tcp.server.summary", name = "enabled", havingValue = "true")
//	SummaryServer summaryServer() {
//		return new SummaryServer(summary.getBindPort());
//	}
}
