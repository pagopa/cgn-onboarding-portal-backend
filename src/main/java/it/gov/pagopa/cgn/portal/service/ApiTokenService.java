package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.apim.AzureApimClient;
import it.gov.pagopa.cgn.portal.enums.ApiTokenTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.model.ApiTokens;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.springframework.stereotype.Service;


@Service
public class ApiTokenService {

    private final AzureApimClient azureApimClient;

    private final ProfileService profileService;

    public ApiTokens getTokens(String agreementId) {
        String merchantTaxCode = extractMerchantTaxCode(agreementId);
        return azureApimClient.getTokens(merchantTaxCode);
    }

    public ApiTokens regenerateToken(String agreementId, String tokenTypeValue) {
        String merchantTaxCode = extractMerchantTaxCode(agreementId);

        ApiTokenTypeEnum tokenType = ApiTokenTypeEnum.fromValue(tokenTypeValue);

        switch (tokenType) {
            case PRIMARY:
                azureApimClient.regeneratePrimaryKey(merchantTaxCode);
                break;
            case SECONDARY:
                azureApimClient.regenerateSecondaryKey(merchantTaxCode);
                break;
            default:
                throw new InvalidRequestException(ErrorCodeEnum.TOKEN_PARAMETER_NOT_VALID.getValue());
        }

        return azureApimClient.getTokens(merchantTaxCode);
    }

    private String extractMerchantTaxCode(String agreementId) {
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        if (profileEntity.getDiscountCodeType() != DiscountCodeTypeEnum.API) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_RETRIEVE_TOKEN_FOR_PROFILE_NOT_API.getValue());
        }

        return profileEntity.getTaxCodeOrVat();
    }


    public ApiTokenService(AzureApimClient azureApimClient, ProfileService profileService) {
        this.azureApimClient = azureApimClient;
        this.profileService = profileService;
    }
}
