package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.security.*;
import it.gov.pagopa.cgn.portal.service.ActiveDirectoryService;
import it.gov.pagopa.cgn.portal.service.OneIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SessionFacade {

    @Autowired
    private OneIdentityService oneIdentityService;

    @Autowired
    private ActiveDirectoryService activeDirectoryService;

    @Autowired
    private JwtUtils jwtUtils;

    public String getOperatorToken(String code, String nonce)
            throws Exception {
        OneIdentityUser oneIdentityUser = oneIdentityService.getOneIdentityUser(code, nonce);
        Map<String, String> claims = new HashMap<>();
        claims.put(JwtClaims.FIRST_NAME.getCode(), oneIdentityUser.getFirstName());
        claims.put(JwtClaims.LAST_NAME.getCode(), oneIdentityUser.getLastName());
        claims.put(JwtClaims.FISCAL_CODE.getCode(), oneIdentityUser.getFiscalCode());
        claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.OPERATOR.getCode());
        return jwtUtils.buildJwtToken(claims);
    }

    public String getAdminToken(String token, String nonce)
            throws Exception {
        ActiveDirectoryUser activeDirectoryUser = activeDirectoryService.getActiveDirectoryUser(token, nonce);
        Map<String, String> claims = new HashMap<>();
        claims.put(JwtClaims.FIRST_NAME.getCode(), activeDirectoryUser.getFirstName());
        claims.put(JwtClaims.LAST_NAME.getCode(), activeDirectoryUser.getLastName());
        claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.ADMIN.getCode());
        return jwtUtils.buildJwtToken(claims);
    }
}
