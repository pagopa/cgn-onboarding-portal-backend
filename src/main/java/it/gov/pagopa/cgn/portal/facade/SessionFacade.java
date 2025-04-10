package it.gov.pagopa.cgn.portal.facade;

import com.nimbusds.oauth2.sdk.ParseException;
import it.gov.pagopa.cgn.portal.security.*;
import it.gov.pagopa.cgn.portal.service.ActiveDirectoryService;
import it.gov.pagopa.cgn.portal.service.OneIdentityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

@Slf4j
@Component
public class SessionFacade {

    private final OneIdentityService oneIdentityService;
    private final ActiveDirectoryService activeDirectoryService;
    private final JwtUtils jwtUtils;

    @Autowired
    public SessionFacade(OneIdentityService oneIdentityService,
                         ActiveDirectoryService activeDirectoryService,
                         JwtUtils jwtUtils) {
        this.oneIdentityService = oneIdentityService;
        this.activeDirectoryService = activeDirectoryService;
        this.jwtUtils = jwtUtils;
    }

    public String getOperatorToken(String code, String nonce)
            throws GeneralSecurityException {
        try {
            OneIdentityUser oneIdentityUser = oneIdentityService.getOneIdentityUser(code, nonce);
            Map<String, String> claims = new HashMap<>();
            claims.put(JwtClaims.FIRST_NAME.getCode(), oneIdentityUser.getFirstName());
            claims.put(JwtClaims.LAST_NAME.getCode(), oneIdentityUser.getLastName());
            claims.put(JwtClaims.FISCAL_CODE.getCode(), oneIdentityUser.getFiscalCode());
            claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.OPERATOR.getCode());
            return jwtUtils.buildJwtToken(claims);
        } catch (URISyntaxException | IOException | ParseException e) {
            log.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
            throw new GeneralSecurityException(e);
        }
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
