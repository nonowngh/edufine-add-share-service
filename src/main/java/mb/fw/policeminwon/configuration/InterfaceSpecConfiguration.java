package mb.fw.policeminwon.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.spec.InterfaceSpecList;

@Profile("proxy")
@Slf4j
@Configuration
public class InterfaceSpecConfiguration {

	private InterfaceSpecList interfaceSpecList;

	@Value("classpath:interface_spec.json")
	Resource jsonFile;

	@Bean
	InterfaceSpecList initInterSpecInfo() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			interfaceSpecList = objectMapper.readValue(jsonFile.getInputStream(), InterfaceSpecList.class);
			log.info("Loading interfaceList -> {}", interfaceSpecList);
		} catch (Exception e) {
			log.error("deserialize interface configure file error! check setting 'interface_spec.json'", e);
		}
		return interfaceSpecList;
	}
}
