package it.gov.pagopa.cgn.portal.config;

import it.gov.pagopa.cgn.portal.interceptor.AgreementInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvConfig implements WebMvcConfigurer {

    @Autowired
    private AgreementInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/agreements/**")
                .excludePathPatterns("/agreements") //create agreements API doesn't have parameter
                .excludePathPatterns("/agreement-requests/**")
                .excludePathPatterns("/approved-agreements**")
                .excludePathPatterns("/geolocation-token/");
    }
}
