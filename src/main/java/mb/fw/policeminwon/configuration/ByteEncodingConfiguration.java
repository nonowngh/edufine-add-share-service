package mb.fw.policeminwon.configuration;

import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.constants.ByteEncodingConstants;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "tcp")
public class ByteEncodingConfiguration {

    private String encoding = "euc-kr";
    
    @PostConstruct
    public void initCharset() {
    	ByteEncodingConstants.CHARSET = Charset.forName(encoding);
        log.info("Setting Default Charset : " + ByteEncodingConstants.CHARSET.name());
    }

}
