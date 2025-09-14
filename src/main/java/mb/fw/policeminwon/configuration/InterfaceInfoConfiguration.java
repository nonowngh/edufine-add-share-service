package mb.fw.policeminwon.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.spec.InterfaceInfoList;

@Profile("proxy")
@Slf4j
@Configuration
public class InterfaceInfoConfiguration {

	private InterfaceInfoList interfaceInfoList;

	@Value("classpath:interface_info.json")
	Resource jsonFile;

	@Bean
	InterfaceInfoList initInterfaceInfo() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			interfaceInfoList = objectMapper.readValue(jsonFile.getInputStream(), InterfaceInfoList.class);
		} catch (Exception e) {
			log.error("deserialize 'interface_info.json' file error!", e);
		}
		return interfaceInfoList;
	}
}
