package it.gov.pagopa.cgn.portal.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Service
public class OpenIdService {

    private final ConfigProperties configProperties;

    @Autowired
    OpenIdService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public String getToken(String code, String state, String nonce)
            throws URISyntaxException, IOException, ParseException {
        AuthorizationCode authCode = new AuthorizationCode(code);
        URI callback = new URI(configProperties.getCgnPortalBaseUrl() + "/session");
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(authCode, callback);

        ClientID clientID = new ClientID(configProperties.getOneIdentityId());
        Secret clientSecret = new Secret(configProperties.getOneIdentitySecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        URI tokenEndpoint = new URI(configProperties.getOneIdentityBaseUrl() + "/token");
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());
        if (!tokenResponse.indicatesSuccess()) {
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            // TODO RETURN ERROR
        }

        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        JWT idToken = successResponse.getOIDCTokens().getIDToken();
        
        return idToken.getParsedString();
    }
}
