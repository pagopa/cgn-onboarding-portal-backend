package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.AttributeAuthorityFacade;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgnonboardingportal.api.OrganizationsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class OrganizationsApiController
        implements OrganizationsApi {

    private final AttributeAuthorityFacade attributeAuthorityFacade;
    private final JwtUtils jwtUtils;

    @Autowired
    public OrganizationsApiController(AttributeAuthorityFacade attributeAuthorityFacade, JwtUtils jwtUtils) {
        this.attributeAuthorityFacade = attributeAuthorityFacade;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public ResponseEntity<List<String>> getOrganizations() {

        String fiscalCode = "form session user";
        return attributeAuthorityFacade.getOrganizations(fiscalCode);
    }
}
