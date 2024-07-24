package it.gov.pagopa.cgn.portal.config;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ApiManagementConfig {

    @Bean
    public ApiManagementManager apiManagementManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder().authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint()).build();
        return ApiManagementManager.authenticate(credential, profile);
    }
}
