package it.gov.pagopa.cgn.portal.apim;


import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.SubscriptionCreateParameters;
import com.azure.resourcemanager.apimanagement.models.Subscriptions;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.model.ApiTokens;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AzureApimClientTest {

    private static final String APIM_RESOURCE_GROUP = "a-resource-group";
    private static final String APIM_RESOURCE = "a-resource";
    private static final String APIM_PRODUCT_ID = "product-id";
    private static final String SUBSCRIPTION_KEY_PREFIX = "cgn";
    private final ConfigProperties configProperties = new ConfigProperties();
    private final ApiManagementManager apiManagementManager = mock(ApiManagementManager.class);
    private final Subscriptions subscriptions = mock(Subscriptions.class);
    private final AzureApimClient azureApimClient = new AzureApimClient(configProperties, apiManagementManager);

    @Before
    public void init() {
        reset(apiManagementManager, subscriptions);

        ReflectionTestUtils.setField(configProperties, "apimResouceGroup", APIM_RESOURCE_GROUP);
        ReflectionTestUtils.setField(configProperties, "apimResouce", APIM_RESOURCE);
        ReflectionTestUtils.setField(configProperties, "apimProductId", APIM_PRODUCT_ID);
        ReflectionTestUtils.setField(configProperties, "apimSubscriptionKeyPrefix", SUBSCRIPTION_KEY_PREFIX);

        when(apiManagementManager.subscriptions()).thenReturn(subscriptions);
    }


    @Test
    public void Get_AlreadyExistingTokens_Ok() {
        when(subscriptions.listSecrets(
                APIM_RESOURCE_GROUP,
                APIM_RESOURCE,
                SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID
        )).thenReturn(TestUtils.createSubscriptionKeysContract());

        ApiTokens actual = azureApimClient.getTokens(TestUtils.FAKE_ID);

        Assert.assertEquals(TestUtils.API_TOKEN_PRIMARY_KEY, actual.getPrimaryToken());
        Assert.assertEquals(TestUtils.API_TOKEN_SECONDARY_KEY, actual.getSecondaryToken());

        verify(subscriptions).listSecrets(anyString(), anyString(), anyString());
    }

    @Test
    public void Create_NewTokensIfNotExisting_Ok() {
        when(subscriptions.listSecrets(
                APIM_RESOURCE_GROUP,
                APIM_RESOURCE,
                SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID
        )).thenThrow(new ManagementException("subscription not found", TestUtils.createEmptyApimHttpResponse(404)));

        when(subscriptions.createOrUpdate(
                eq(APIM_RESOURCE_GROUP),
                eq(APIM_RESOURCE),
                eq(SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID),
                any(SubscriptionCreateParameters.class)
        )).thenReturn(TestUtils.createSubscriptionContract());

        ApiTokens actual = azureApimClient.getTokens(TestUtils.FAKE_ID);

        Assert.assertEquals(TestUtils.API_TOKEN_PRIMARY_KEY, actual.getPrimaryToken());
        Assert.assertEquals(TestUtils.API_TOKEN_SECONDARY_KEY, actual.getSecondaryToken());

        verify(subscriptions).listSecrets(anyString(), anyString(), anyString());
        verify(subscriptions).createOrUpdate(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void RegeneratePrimaryKey_Ok() {
        doNothing().when(subscriptions).regeneratePrimaryKey(
                APIM_RESOURCE_GROUP,
                APIM_RESOURCE,
                SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID
        );

        azureApimClient.regeneratePrimaryKey(TestUtils.FAKE_ID);

        verify(subscriptions).regeneratePrimaryKey(
                APIM_RESOURCE_GROUP,
                APIM_RESOURCE,
                SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID
        );
    }

    @Test
    public void RegenerateSecodaryKey_Ok() {
        doNothing().when(subscriptions).regenerateSecondaryKey(
                APIM_RESOURCE_GROUP,
                APIM_RESOURCE,
                SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID
        );

        azureApimClient.regenerateSecondaryKey(TestUtils.FAKE_ID);

        verify(subscriptions).regenerateSecondaryKey(
                APIM_RESOURCE_GROUP,
                APIM_RESOURCE,
                SUBSCRIPTION_KEY_PREFIX + "-" + TestUtils.FAKE_ID
        );
    }

}
