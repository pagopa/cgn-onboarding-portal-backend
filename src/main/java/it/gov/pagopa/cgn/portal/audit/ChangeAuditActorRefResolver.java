package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ChangeAuditActorRefResolver {

    public String resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            return null;
        }

        Object principal = token.getPrincipal();
        if (principal instanceof JwtAdminUser adminUser) {
            return adminUser.getUserFullName();
        }
        if (principal instanceof JwtOperatorUser operatorUser) {
            return operatorUser.getUserTaxCode();
        }

        return null;
    }
}