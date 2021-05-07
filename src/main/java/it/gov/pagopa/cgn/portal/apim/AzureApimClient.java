package it.gov.pagopa.cgn.portal.apim;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.*;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.model.ApiTokens;
import org.springframework.stereotype.Service;

@Service
public class AzureApimClient {


    private ConfigProperties configProperties;

    private AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
    private TokenCredential credential = new DefaultAzureCredentialBuilder().authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint()).build();
    private ApiManagementManager manager = ApiManagementManager.authenticate(credential, profile);


    public AzureApimClient(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public ApiTokens getTokens(String merchantTaxCode) {
        Subscriptions subscriptionApi = manager.subscriptions();

        try {
            SubscriptionKeysContract subscriptionKeys = subscriptionApi.listSecrets(
                    configProperties.getApimResouceGroup(),
                    configProperties.getApimResouce(),
                    getApimSubscriptionKey(merchantTaxCode)
            );

            ApiTokens tokens = new ApiTokens();
            tokens.setPrimaryToken(subscriptionKeys.primaryKey());
            tokens.setSecondaryToken(subscriptionKeys.secondaryKey());

            return tokens;

        } catch (ManagementException exc) {
            if (exc.getResponse().getStatusCode() == 404) {
                return createSubscription(merchantTaxCode);
            }
            throw exc;

        }
    }

    public void regeneratePrimaryKey(String merchantTaxCode) {
        Subscriptions subscriptionApi = manager.subscriptions();
        subscriptionApi.regeneratePrimaryKey(configProperties.getApimResouceGroup(), configProperties.getApimResouce(), getApimSubscriptionKey(merchantTaxCode));
    }

    public void regenerateSecondaryKey(String merchantTaxCode) {
        Subscriptions subscriptionApi = manager.subscriptions();
        subscriptionApi.regenerateSecondaryKey(configProperties.getApimResouceGroup(), configProperties.getApimResouce(), getApimSubscriptionKey(merchantTaxCode));
    }

    private ApiTokens createSubscription(String merchantTaxCode) {
        Subscriptions subscriptionApi = manager.subscriptions();

        SubscriptionCreateParameters subscriptionCreateParameters = new SubscriptionCreateParameters()
                .withScope("/products/" + configProperties.getApimProductId())
                .withState(SubscriptionState.ACTIVE)
                .withDisplayName(getApimSubscriptionKey(merchantTaxCode));

        System.out.println("PRAMS-> " + configProperties.getApimProductId());
        System.out.println("PRAMS-> " + getApimSubscriptionKey(merchantTaxCode));
        System.out.println("PRAMS-> " + configProperties.getApimResouceGroup());
        System.out.println("PRAMS-> " + configProperties.getApimResouce());

        SubscriptionContract subscription = subscriptionApi.createOrUpdate(
                configProperties.getApimResouceGroup(),
                configProperties.getApimResouce(),
                getApimSubscriptionKey(merchantTaxCode),
                subscriptionCreateParameters
        );


        ApiTokens tokens = new ApiTokens();
        tokens.setPrimaryToken(subscription.primaryKey());
        tokens.setSecondaryToken(subscription.secondaryKey());

        return tokens;
    }

    private String getApimSubscriptionKey(String merchantTaxCode) {
        return configProperties.getApimSubscriptionKeyPrefix() + "-" + merchantTaxCode;
    }

}
