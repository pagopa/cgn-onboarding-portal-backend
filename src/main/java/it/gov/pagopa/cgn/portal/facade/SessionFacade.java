package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.security.CgnUserRoleEnum;
import it.gov.pagopa.cgn.portal.security.JwtClaims;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgn.portal.security.OneIdentityUser;
import it.gov.pagopa.cgn.portal.service.OneIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SessionFacade {

    private final OneIdentityService oneIdentityService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    public SessionFacade(OneIdentityService oneIdentityService) {
        this.oneIdentityService = oneIdentityService;
    }

    public String getOperatorToken(String code, String state, String nonce)
            throws Exception {
        OneIdentityUser oneIdentityUser = oneIdentityService.getOneIdentityUser(code, state, nonce);
        Map<String, String> claims = new HashMap<>();
        claims.put(JwtClaims.FIRST_NAME.getCode(), oneIdentityUser.getFirstName());
        claims.put(JwtClaims.LAST_NAME.getCode(), oneIdentityUser.getLastName());
        claims.put(JwtClaims.FISCAL_CODE.getCode(), oneIdentityUser.getFiscalCode());
        claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.OPERATOR.getCode());
        return jwtUtils.buildJwtToken(claims);
    }

    public String getAdminToken(String token)
            throws Exception {
        return "TBD";
    }
}
