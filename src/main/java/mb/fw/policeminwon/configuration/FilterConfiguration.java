package mb.fw.policeminwon.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import mb.fw.policeminwon.filter.ServletLoggingFilter;
import mb.fw.policeminwon.spec.InterfaceSpecList;

@Configuration
public class FilterConfiguration {

	@Bean
    FilterRegistrationBean<ServletLoggingFilter> servletLoggingFilter(InterfaceSpecList interfaceSpecList,
			@Autowired(required = false) JmsTemplate jmsTemplate) {
        FilterRegistrationBean<ServletLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ServletLoggingFilter(interfaceSpecList, jmsTemplate));
        registrationBean.addUrlPatterns("/esb/api/proxy/*");
        return registrationBean;
    }
}
