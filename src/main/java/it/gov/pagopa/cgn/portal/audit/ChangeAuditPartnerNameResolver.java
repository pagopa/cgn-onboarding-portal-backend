package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ChangeAuditPartnerNameResolver {

    public String resolveForAgreement(AgreementEntity agreement) {
        ProfileEntity profile = agreement.getProfile();
        if (profile != null && StringUtils.hasText(profile.getFullName())) {
            return profile.getFullName();
        }
        return agreement.getOrganizationName();
    }
}