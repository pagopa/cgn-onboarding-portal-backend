package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.AttributeAuthorityApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.DefaultApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class AttributeAuthorityService {

    private final AttributeAuthorityApi attributeAuthorityApi;

    private final DefaultApi defaultAttributeAuthorityApi;

    private final ConfigProperties configProperties;

    public AttributeAuthorityService(ConfigProperties configProperties,
                                     AttributeAuthorityApi attributeAuthorityApi,
                                     DefaultApi defaultAttributeAuthorityApi) {
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

    public List<CompanyAttributeAuthority> getAgreementOrganizations(String referentFiscalCode)
            throws HttpClientErrorException {
        return defaultAttributeAuthorityApi.getUserCompanies(new GetCompaniesBodyAttributeAuthority().fiscalCode(
                referentFiscalCode));
    }

    public int countUserOrganizations(String referentFiscalCode)
            throws HttpClientErrorException {
        GetCompaniesBodyAttributeAuthority body = new GetCompaniesBodyAttributeAuthority();
        body.setFiscalCode(referentFiscalCode);
        return defaultAttributeAuthorityApi.getUserCompanies(body).size();
    }
}
