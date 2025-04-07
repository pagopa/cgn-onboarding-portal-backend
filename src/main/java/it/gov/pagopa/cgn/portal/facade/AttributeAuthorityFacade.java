package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.CompanyAttributeAuthority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Component
public class AttributeAuthorityFacade {

    private final AgreementService agreementService;
    private final AgreementUserService agreementUserService;
    private final AttributeAuthorityService attributeAuthorityService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AttributeAuthorityFacade(AttributeAuthorityService attributeAuthorityService,
                                    AgreementService agreementService,
                                    AgreementUserService agreementUserService,
                                    JwtUtils jwtUtils) {
        this.agreementUserService = agreementUserService;
        this.attributeAuthorityService = attributeAuthorityService;
        this.agreementService = agreementService;
        this.jwtUtils = jwtUtils;
    }

    public ResponseEntity<List<String>> getOrganizations() {
        String merchantTaxCode = CGNUtils.getJwtOperatorUserId();

        List<CompanyAttributeAuthority> companies = attributeAuthorityService.getAgreementOrganizations(merchantTaxCode);

        List<String> tokens = companies.stream().map(company -> {
            try {
                AgreementEntity ae = agreementService.getAgreementByMerchantTaxCode(merchantTaxCode);
                return jwtUtils.buildJwtToken(jwtUtils.getClaimsForAttributeAuthorityCompany(company, ae.getId()));
            } catch (GeneralSecurityException e) {
                throw new InternalErrorException(e.getMessage());
            }
        }).toList();

        return ResponseEntity.ok(tokens);
    }
}
