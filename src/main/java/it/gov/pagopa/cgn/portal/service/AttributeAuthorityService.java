package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.AttributeAuthorityApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsPostAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AttributeAuthorityService {

    private final AttributeAuthorityApi attributeAuthorityApi;

    public AttributeAuthorityService(ConfigProperties configProperties, AttributeAuthorityApi attributeAuthorityApi) {
        this.attributeAuthorityApi = attributeAuthorityApi;
        this.attributeAuthorityApi.getApiClient().setBasePath(configProperties.getAttributeAuthorityBaseUrl());
    }

    public ResponseEntity<OrganizationsAttributeAuthority> getOrganizations(String searchQuery, Integer page, Integer pageSize, String sortBy, String sortDirection) {
        return attributeAuthorityApi.getOrganizationsWithHttpInfo(searchQuery, page, pageSize, sortBy, sortDirection);
    }

    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> upsertOrganization(OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsAttributeAuthority) {
        return attributeAuthorityApi.upsertOrganizationWithHttpInfo(organizationWithReferentsAttributeAuthority);
    }

    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> getOrganization(String keyOrganizationFiscalCode) {
        return attributeAuthorityApi.getOrganizationWithHttpInfo(keyOrganizationFiscalCode);
    }

    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        return attributeAuthorityApi.deleteOrganizationWithHttpInfo(keyOrganizationFiscalCode);
    }
}
