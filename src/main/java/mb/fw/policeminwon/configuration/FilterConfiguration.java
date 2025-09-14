package mb.fw.policeminwon.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mb.fw.policeminwon.filter.ServletLoggingFilter;

@Configuration
public class FilterConfiguration {

	@Bean
    FilterRegistrationBean<ServletLoggingFilter> loggingFilter() {
        FilterRegistrationBean<ServletLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ServletLoggingFilter());
        registrationBean.addUrlPatterns("/esb/api/proxy/*");  // 특정 URL 패턴에만 필터 적용
        return registrationBean;
    }
}
