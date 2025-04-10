package it.gov.pagopa.cgn.portal.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

@Slf4j
public class OidcJwtValidation {

    private final RestTemplate restTemplate;
    private final URI wellKnownUri;
    private final ClientID clientId;
    private final JWSAlgorithm jwsAlg;
    private Issuer iss;
    private URI jwkSetURI;

    public OidcJwtValidation(String wellKnownUri, String clientId)
            throws JsonProcessingException {
        this.restTemplate = new RestTemplate();
        this.wellKnownUri = URI.create(wellKnownUri);
        this.clientId = new ClientID(clientId);
        this.jwsAlg = JWSAlgorithm.RS256;
        getWellKnownData();
    }

    public IDTokenClaimsSet validateIdTokenAndGetClaims(JWT idToken, String nonce)
            throws MalformedURLException {
        IDTokenValidator validator = new IDTokenValidator(iss, clientId, jwsAlg, jwkSetURI.toURL());
        Nonce expectedNonce = new Nonce(nonce);
        IDTokenClaimsSet claims = null;
        try {
            claims = validator.validate(idToken, expectedNonce);
        } catch (BadJOSEException e) {
            log.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
            throw new InternalErrorException("Invalid signature or data.");
        } catch (JOSEException e) {
            log.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
            throw new InternalErrorException("Internal server error during idToken validation.");
        }
        return claims;
    }

    private String getRequest(URI uri) {
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new RuntimeException("Cannot GET: " + uri);
        return response.getBody();
    }

    private void getWellKnownData()
            throws JsonProcessingException {
        String wellKnownData = getRequest(wellKnownUri);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode wellKnownJSON = mapper.readTree(wellKnownData);
        jwkSetURI = URI.create(wellKnownJSON.get("jwks_uri").asText());
        iss = new Issuer(wellKnownJSON.get("issuer").asText());
    }
}


