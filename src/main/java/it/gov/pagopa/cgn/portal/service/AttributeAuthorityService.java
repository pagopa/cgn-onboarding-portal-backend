package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.*;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;


@Service
public class AttributeAuthorityService {

    private final AttributeAuthorityApi attributeAuthorityApi;

    private final DefaultApi defaultAttributeAuthorityApi;

    private final ConfigProperties configProperties;

    public AttributeAuthorityService(ConfigProperties configProperties, AttributeAuthorityApi attributeAuthorityApi, DefaultApi defaultAttributeAuthorityApi) {
        this.attributeAuthorityApi = attributeAuthorityApi;
        this.defaultAttributeAuthorityApi = defaultAttributeAuthorityApi;
        this.configProperties = configProperties;
    }

    @PostConstruct
    public void setup() {
        this.attributeAuthorityApi.getApiClient().setBasePath(configProperties.getAttributeAuthorityBaseUrl());
    }

    public ResponseEntity<OrganizationsAttributeAuthority> getOrganizations(String searchQuery,
                                                                            Integer page,
                                                                            Integer pageSize,
                                                                            String sortBy,
                                                                            String sortDirection) {
        return attributeAuthorityApi.getOrganizationsWithHttpInfo(searchQuery, page, pageSize, sortBy, sortDirection);
    }

    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> upsertOrganization(
            OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsAttributeAuthority) {
        return attributeAuthorityApi.upsertOrganizationWithHttpInfo(organizationWithReferentsAttributeAuthority);
    }

    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> getOrganization(String keyOrganizationFiscalCode) {
        return attributeAuthorityApi.getOrganizationWithHttpInfo(keyOrganizationFiscalCode);
    }

    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        return attributeAuthorityApi.deleteOrganizationWithHttpInfo(keyOrganizationFiscalCode);
    }

    public ResponseEntity<List<String>> getReferents(String keyOrganizationFiscalCode) {
        return attributeAuthorityApi.getReferentsWithHttpInfo(keyOrganizationFiscalCode);
    }

    public ResponseEntity<Void> insertReferent(String keyOrganizationFiscalCode,
                                               ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority) {
        return attributeAuthorityApi.insertReferentWithHttpInfo(keyOrganizationFiscalCode,
                referentFiscalCodeAttributeAuthority);
    }

    public ResponseEntity<Void> deleteReferent(String keyOrganizationFiscalCode, String referentFiscalCode) {
        return attributeAuthorityApi.deleteReferentWithHttpInfo(keyOrganizationFiscalCode, referentFiscalCode);
    }

    public int countUserOrganizations(String referentFiscalCode) {
        GetCompaniesBodyAttributeAuthority body = new GetCompaniesBodyAttributeAuthority();
        body.setFiscalCode(referentFiscalCode);
        return defaultAttributeAuthorityApi.getUserCompanies(body).size();
    }

}
