package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.AttributeAuthorityFacade;
import it.gov.pagopa.cgnonboardingportal.api.ReferentOrganizationsApi;
import it.gov.pagopa.cgnonboardingportal.model.Organizations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ROLE_MERCHANT')")
public class ReferentOrganizationsController
        implements ReferentOrganizationsApi {

    private final AttributeAuthorityFacade attributeAuthorityFacade;

    @Autowired
    public ReferentOrganizationsController(AttributeAuthorityFacade attributeAuthorityFacade) {
        this.attributeAuthorityFacade = attributeAuthorityFacade;
    }

    @Override
    public ResponseEntity<Organizations> getOrganizations() {
        return attributeAuthorityFacade.getOrganizations();
    }
}
