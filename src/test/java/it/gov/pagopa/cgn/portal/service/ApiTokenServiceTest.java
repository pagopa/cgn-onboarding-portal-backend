package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.apim.AzureApimClient;
import it.gov.pagopa.cgn.portal.enums.ApiTokenTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.model.ApiTokens;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

import static org.mockito.Mockito.*;


public class ApiTokenServiceTest {

    private final AzureApimClient azureApimClient = mock(AzureApimClient.class);
    private final ProfileService profileService = mock(ProfileService.class);

    private final ApiTokenService apiTokenService = new ApiTokenService(azureApimClient, profileService);

    @Before
    public void init() {
        reset(azureApimClient, profileService);
    }


    @Test
    public void Get_getApiTokens_Ok() {
        ProfileEntity pe = TestUtils.createSampleProfileWithCommonFields();
        pe.setTaxCodeOrVat("sample-tax-code");
        pe.setDiscountCodeType(DiscountCodeTypeEnum.API);

        ApiTokens expected = TestUtils.createSampleApiTokens();

        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.of(pe));
        when(azureApimClient.getTokens(eq(pe.getTaxCodeOrVat()))).thenReturn(expected);

        ApiTokens actualTokens = apiTokenService.getTokens(TestUtils.FAKE_ID);

        Assert.assertEquals(expected, actualTokens);

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient).getTokens(eq(pe.getTaxCodeOrVat()));
    }

    @Test
    public void Get_getApiTokensWithoutProfile_ThrowsException() {
        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(InvalidRequestException.class, () -> apiTokenService.getTokens(TestUtils.FAKE_ID));

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient, never()).getTokens(anyString());
    }

    @Test
    public void Get_getApiTokensWithStaticCode_ThrowsException() {
        ProfileEntity pe = TestUtils.createSampleProfileWithCommonFields();
        pe.setTaxCodeOrVat("sample-tax-code");
        pe.setDiscountCodeType(DiscountCodeTypeEnum.STATIC);

        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.of(pe));

        Assertions.assertThrows(InvalidRequestException.class, () -> apiTokenService.getTokens(TestUtils.FAKE_ID));

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient, never()).getTokens(anyString());
    }

    @Test
    public void Regenerate_PrimaryApiToken_Ok() {
        ProfileEntity pe = TestUtils.createSampleProfileWithCommonFields();
        pe.setTaxCodeOrVat("sample-tax-code");
        pe.setDiscountCodeType(DiscountCodeTypeEnum.API);

        ApiTokens expected = TestUtils.createSampleApiTokens();

        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.of(pe));
        doNothing().when(azureApimClient).regeneratePrimaryKey(eq(pe.getTaxCodeOrVat()));
        when(azureApimClient.getTokens(eq(pe.getTaxCodeOrVat()))).thenReturn(expected);

        ApiTokens actualTokens = apiTokenService.regenerateToken(TestUtils.FAKE_ID, "primary");

        Assert.assertEquals(expected, actualTokens);

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient).regeneratePrimaryKey(pe.getTaxCodeOrVat());
        verify(azureApimClient).getTokens(eq(pe.getTaxCodeOrVat()));
    }

    @Test
    public void Regenerate_SecondaryApiToken_Ok() {
        ProfileEntity pe = TestUtils.createSampleProfileWithCommonFields();
        pe.setTaxCodeOrVat("sample-tax-code");
        pe.setDiscountCodeType(DiscountCodeTypeEnum.API);

        ApiTokens expected = TestUtils.createSampleApiTokens();

        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.of(pe));
        doNothing().when(azureApimClient).regenerateSecondaryKey(eq(pe.getTaxCodeOrVat()));
        when(azureApimClient.getTokens(eq(pe.getTaxCodeOrVat()))).thenReturn(expected);

        ApiTokens actualTokens = apiTokenService.regenerateToken(TestUtils.FAKE_ID, "secondary");

        Assert.assertEquals(expected, actualTokens);

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient).regenerateSecondaryKey(pe.getTaxCodeOrVat());
        verify(azureApimClient).getTokens(eq(pe.getTaxCodeOrVat()));
    }

    @Test
    public void Get_regenerateTokensWithoutProfile_ThrowsException() {
        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(InvalidRequestException.class, () -> apiTokenService.regenerateToken(TestUtils.FAKE_ID, ApiTokenTypeEnum.PRIMARY.getCode()));

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient, never()).getTokens(anyString());
    }

    @Test
    public void Get_regenerateTokensWithStaticCode_ThrowsException() {
        ProfileEntity pe = TestUtils.createSampleProfileWithCommonFields();
        pe.setTaxCodeOrVat("sample-tax-code");
        pe.setDiscountCodeType(DiscountCodeTypeEnum.STATIC);

        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.of(pe));

        Assertions.assertThrows(InvalidRequestException.class, () -> apiTokenService.regenerateToken(TestUtils.FAKE_ID, ApiTokenTypeEnum.PRIMARY.getCode()));

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient, never()).getTokens(anyString());
    }

    @Test
    public void Regenerate_ApiTokenWithInvalidTokenType_ThrowsException() {
        ProfileEntity pe = TestUtils.createSampleProfileWithCommonFields();
        pe.setTaxCodeOrVat("sample-tax-code");
        pe.setDiscountCodeType(DiscountCodeTypeEnum.API);

        when(profileService.getProfile(TestUtils.FAKE_ID)).thenReturn(Optional.of(pe));

        Assertions.assertThrows(InvalidRequestException.class, () -> apiTokenService.regenerateToken(TestUtils.FAKE_ID, "wrong-token-type"));

        verify(profileService).getProfile(TestUtils.FAKE_ID);
        verify(azureApimClient, never()).regenerateSecondaryKey(anyString());
        verify(azureApimClient, never()).getTokens(anyString());
    }
}
