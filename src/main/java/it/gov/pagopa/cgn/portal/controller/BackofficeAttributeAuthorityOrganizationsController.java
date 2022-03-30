package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgnonboardingportal.backoffice.api.OrganizationsApi;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organizations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;


@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class BackofficeAttributeAuthorityOrganizationsController implements OrganizationsApi {

    private final BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade;

    @Autowired
    public BackofficeAttributeAuthorityOrganizationsController(BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade) {
        this.backofficeAttributeAuthorityFacade = backofficeAttributeAuthorityFacade;
    }

    @Override
    public ResponseEntity<Organizations> getOrganizations(String searchQuery, Integer page, Integer pageSize, String sortBy, String sortDirection) {
        return backofficeAttributeAuthorityFacade.getOrganizations(searchQuery, page, pageSize, sortBy, sortDirection);
    }

    @Override
    public ResponseEntity<OrganizationWithReferents> upsertOrganization(OrganizationWithReferents body) {
        return backofficeAttributeAuthorityFacade.upsertOrganization(body);
    }
}
