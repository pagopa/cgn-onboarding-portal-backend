package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsPostConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationsConverter;
import it.gov.pagopa.cgn.portal.service.ApprovedAgreementService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.service.BackofficeAgreementService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organizations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BackofficeAttributeAuthorityFacade {

    private final AttributeAuthorityService attributeAuthorityService;

    private final BackofficeAgreementService backofficeAgreementService;

    private final ApprovedAgreementService approvedAgreementService;

    private final OrganizationsConverter organizationsConverter;

    private final OrganizationConverter organizationConverter;

    private final OrganizationWithReferentsConverter organizationWithReferentsConverter;

    private final OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter;

    public ResponseEntity<Organizations> getOrganizations(String searchQuery, Integer page, Integer pageSize, String sortBy, String sortDirection) {
        return ResponseEntity.ok(organizationsConverter.fromAttributeAuthorityModel(attributeAuthorityService.getOrganizations(searchQuery, page, pageSize, sortBy, sortDirection)));
    }

    public ResponseEntity<OrganizationWithReferents> getOrganization(String keyOrganizationFiscalCode) {
        try {
            return ResponseEntity.ok(organizationWithReferentsConverter.fromAttributeAuthorityModel(attributeAuthorityService.getOrganization(keyOrganizationFiscalCode)));
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<OrganizationWithReferents> upsertOrganization(OrganizationWithReferents organizationWithReferents) {
        return ResponseEntity.ok(organizationWithReferentsConverter.fromAttributeAuthorityModel(attributeAuthorityService.upsertOrganization(organizationWithReferentsPostConverter.toAttributeAuthorityModel(organizationWithReferents))));
    }

    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        try {
            attributeAuthorityService.deleteOrganization(keyOrganizationFiscalCode);
            return ResponseEntity.noContent().build();
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Autowired
    public BackofficeAttributeAuthorityFacade(AttributeAuthorityService attributeAuthorityService,
                                              BackofficeAgreementService backofficeAgreementService,
                                              ApprovedAgreementService approvedAgreementService,
                                              OrganizationsConverter organizationsConverter,
                                              OrganizationConverter organizationConverter,
                                              OrganizationWithReferentsConverter organizationWithReferentsConverter,
                                              OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter) {
        this.attributeAuthorityService = attributeAuthorityService;
        this.backofficeAgreementService = backofficeAgreementService;
        this.approvedAgreementService = approvedAgreementService;
        this.organizationsConverter = organizationsConverter;
        this.organizationConverter = organizationConverter;
        this.organizationWithReferentsConverter = organizationWithReferentsConverter;
        this.organizationWithReferentsPostConverter = organizationWithReferentsPostConverter;
    }
}
