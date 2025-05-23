package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.security.CgnUserRoleEnum;
import it.gov.pagopa.cgn.portal.security.JwtClaims;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.CompanyAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.model.Organization;
import it.gov.pagopa.cgnonboardingportal.model.Organizations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class AttributeAuthorityFacade {

    private final AttributeAuthorityService attributeAuthorityService;

    private final JwtUtils jwtUtils;

    @Autowired
    public AttributeAuthorityFacade(JwtUtils jwtUtils, AttributeAuthorityService attributeAuthorityService) {
        this.jwtUtils = jwtUtils;
        this.attributeAuthorityService = attributeAuthorityService;
    }

    public ResponseEntity<Organizations> getOrganizations() {
        String operatorFiscalCode = CGNUtils.getJwtOperatorFiscalCode();
        try {
            List<CompanyAttributeAuthority> companies = attributeAuthorityService.getAgreementOrganizations(
                    operatorFiscalCode);

            Organizations organizations = new Organizations(companies.stream().map(company -> {
                HashMap<String, String> claims = new HashMap<>();
                claims.put(JwtClaims.FIRST_NAME.getCode(), CGNUtils.getJwtOperatorFirstName());
                claims.put(JwtClaims.LAST_NAME.getCode(), CGNUtils.getJwtOperatorLastName());
                claims.put(JwtClaims.FISCAL_CODE.getCode(), CGNUtils.getJwtOperatorFiscalCode());
                claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.OPERATOR.getCode());
                claims.put(JwtClaims.ORGANIZATION_FISCAL_CODE.getCode(), company.getFiscalCode());
                String organizationToken = jwtUtils.buildJwtToken(claims);
                Organization organization = new Organization();
                organization.setOrganizationName(company.getOrganizationName());
                organization.setEmail(company.getPec());
                organization.setOrganizationFiscalCode(company.getFiscalCode());
                organization.setToken(organizationToken);
                return organization;
            }).toList());

            return ResponseEntity.ok(organizations);
        } catch (HttpClientErrorException e) {
            // if we have exception we return empty list
            return ResponseEntity.ok(new Organizations());
        }
    }
}
