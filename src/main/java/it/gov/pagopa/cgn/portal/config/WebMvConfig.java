package it.gov.pagopa.cgn.portal.config;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import it.gov.pagopa.cgn.portal.interceptor.AgreementInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
                .excludePathPatterns("/approved-agreements**");
    }

    @Bean
    public ApiManagementManager apiManagementManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder().authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint()).build();
        return ApiManagementManager.authenticate(credential, profile);
    }
}
