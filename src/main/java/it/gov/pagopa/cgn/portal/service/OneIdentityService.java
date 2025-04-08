package it.gov.pagopa.cgn.portal.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.security.OidcJwtValidation;
import it.gov.pagopa.cgn.portal.security.OneIdentityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Service
public class OneIdentityService {

    private final ConfigProperties configProperties;

    @Autowired
    OneIdentityService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public OneIdentityUser getOneIdentityUser(String code, String nonce)
            throws URISyntaxException, IOException, ParseException {
        TokenRequest request = getTokenRequest(code);
        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());
        if (!tokenResponse.indicatesSuccess()) {
            throw new RuntimeException(
                    "Cannot validate OIDC AuthCode: " + tokenResponse.toErrorResponse().toJSONObject().toJSONString());
        }
        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        JWT idToken = successResponse.getOIDCTokens().getIDToken();
        OidcJwtValidation validator = new OidcJwtValidation(configProperties.getOneIdentityWellKnown(),
                                                            configProperties.getOneIdentityId());
        IDTokenClaimsSet claims = validator.validateIdTokenAndGetClaims(idToken, nonce);
        return new OneIdentityUser(claims.getStringClaim("fiscalNumber").replace("TINIT-", ""),
                                   claims.getStringClaim("name"),
                                   claims.getStringClaim("familyName"));
    }

    private TokenRequest getTokenRequest(String code)
            throws URISyntaxException {
        ClientID clientID = new ClientID(configProperties.getOneIdentityId());
        Secret clientSecret = new Secret(configProperties.getOneIdentitySecret());
        URI callback = new URI(configProperties.getCgnPortalBaseUrl() + "/session");
        URI tokenEndpoint = new URI(configProperties.getOneIdentityBaseUrl() + "/oidc/token");
        AuthorizationCode authCode = new AuthorizationCode(code);
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(authCode, callback);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        return new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
    }
}
