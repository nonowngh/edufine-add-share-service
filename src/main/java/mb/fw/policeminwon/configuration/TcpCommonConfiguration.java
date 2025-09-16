package mb.fw.policeminwon.configuration;

import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.constants.TcpMessageLoggingConstants;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "tcp")
public class TcpCommonConfiguration {

	private String encoding = "euc-kr";
	private boolean prettyLogging = true;

	@PostConstruct
	public void init() {
		// set Charset...
		ByteEncodingConstants.CHARSET = Charset.forName(encoding);
		log.info("Setting Default Charset : " + ByteEncodingConstants.CHARSET.name());
		// set tcp message pretty logging...
		TcpMessageLoggingConstants.prettyLogging = prettyLogging;
	}

}
