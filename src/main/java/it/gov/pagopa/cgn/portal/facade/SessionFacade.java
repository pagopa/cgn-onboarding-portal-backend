package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.security.CgnUserRoleEnum;
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

    public String getToken(String code, String state, String nonce)
            throws Exception {
        OneIdentityUser oneIdentityUser = oneIdentityService.getOneIdentityUser(code, state, nonce);
        Map<String, String> claims = new HashMap<>();
        claims.put("first_name", oneIdentityUser.getFirstName());
        claims.put("last_name", oneIdentityUser.getLastName());
        claims.put("fiscal_code", oneIdentityUser.getFiscalCode());
        claims.put("role", CgnUserRoleEnum.OPERATOR.toString());
        return jwtUtils.buildJwtToken(claims);
    }
}
