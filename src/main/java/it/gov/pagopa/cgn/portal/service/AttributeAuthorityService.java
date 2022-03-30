package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.DefaultApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import org.springframework.stereotype.Service;


@Service
public class AttributeAuthorityService {

    private final DefaultApi defaultApi;

    public AttributeAuthorityService(ConfigProperties configProperties) {
        this.defaultApi = new DefaultApi();
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(configProperties.getAttributeAuthorityBaseUrl());
        defaultApi.setApiClient(apiClient);
    }

    public OrganizationsAttributeAuthority getOrganizations(String searchQuery, Integer page, Integer pageSize, String sortBy, String sortDirection) {
        return defaultApi.getOrganizations(searchQuery, page, pageSize, sortBy, sortDirection);
    }

    public OrganizationWithReferentsAttributeAuthority upsertOrganization(OrganizationWithReferentsAttributeAuthority organizationWithReferentsAttributeAuthority) {
        return defaultApi.upsertOrganization(organizationWithReferentsAttributeAuthority);
    }

    public OrganizationWithReferentsAttributeAuthority getOrganization(String keyOrganizationFiscalCode) {
        return defaultApi.getOrganization(keyOrganizationFiscalCode);
    }

    public void deleteOrganization(String keyOrganizationFiscalCode) {
        defaultApi.deleteOrganization(keyOrganizationFiscalCode);
    }
}
