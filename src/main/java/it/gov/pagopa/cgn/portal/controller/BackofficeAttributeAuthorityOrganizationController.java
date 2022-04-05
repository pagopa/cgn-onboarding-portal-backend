package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgnonboardingportal.backoffice.api.OrganizationApi;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ReferentFiscalCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class BackofficeAttributeAuthorityOrganizationController implements OrganizationApi {

    private final BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade;

    @Autowired
    public BackofficeAttributeAuthorityOrganizationController(BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade) {
        this.backofficeAttributeAuthorityFacade = backofficeAttributeAuthorityFacade;
    }


    @Override
    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        return backofficeAttributeAuthorityFacade.deleteOrganization(keyOrganizationFiscalCode);
    }

    @Override
    public ResponseEntity<Void> deleteReferent(String keyOrganizationFiscalCode, String referentFiscalCode) {
        return OrganizationApi.super.deleteReferent(keyOrganizationFiscalCode, referentFiscalCode);
    }

    @Override
    public ResponseEntity<OrganizationWithReferents> getOrganization(String keyOrganizationFiscalCode) {
        return backofficeAttributeAuthorityFacade.getOrganization(keyOrganizationFiscalCode);
    }


    @Override
    public ResponseEntity<List<String>> getReferents(String keyOrganizationFiscalCode) {
        return OrganizationApi.super.getReferents(keyOrganizationFiscalCode);
    }

    @Override
    public ResponseEntity<Void> insertReferent(String keyOrganizationFiscalCode, ReferentFiscalCode body) {
        return OrganizationApi.super.insertReferent(keyOrganizationFiscalCode, body);
    }
}
