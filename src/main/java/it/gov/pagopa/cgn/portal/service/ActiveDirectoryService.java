package it.gov.pagopa.cgn.portal.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.security.ActiveDirectoryUser;
import it.gov.pagopa.cgn.portal.security.OidcJwtValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Service
public class ActiveDirectoryService {

    private final ConfigProperties configProperties;

    @Autowired
    public ActiveDirectoryService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public ActiveDirectoryUser getActiveDirectoryUser(String token, String nonce)
            throws IOException, java.text.ParseException {
        OidcJwtValidation validator = new OidcJwtValidation(configProperties.getActiveDirectoryWellKnown(),
                                                            configProperties.getActiveDirectoryId());
        JWT idToken = JWTParser.parse(token);
        IDTokenClaimsSet claims = validator.validateIdTokenAndGetClaims(idToken, nonce);
        return new ActiveDirectoryUser(claims.getStringClaim("given_name"), claims.getStringClaim("family_name"));
    }
}
