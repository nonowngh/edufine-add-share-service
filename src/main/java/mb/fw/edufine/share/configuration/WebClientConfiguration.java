package mb.fw.edufine.share.configuration;

import javax.net.ssl.SSLException;

import org.apache.http.HttpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "web.client", ignoreUnknownFields = true)
@ConditionalOnProperty(name = "web.client.enabled", havingValue = "true")
public class WebClientConfiguration {

	private String targetUrl;

	@Bean(name = "webClient")
	WebClient webClient() {
		if (targetUrl.startsWith("https://")) {
			HttpClient httpClient = HttpClient.create().secure(ssl -> {
				try {
					ssl.sslContext(
							SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build());
				} catch (SSLException e) {
					log.error("sslContext error!");
				}
			});
			return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl(targetUrl)
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
		}
		return WebClient.builder().baseUrl(targetUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
	}
}
