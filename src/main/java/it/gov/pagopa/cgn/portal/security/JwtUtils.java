package it.gov.pagopa.cgn.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    private final ConfigProperties configProperties;

    @Autowired
    public JwtUtils(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * Build a signed token with the given claims that expires in 12 hours
     *
     * @param claims
     * @return
     * @throws GeneralSecurityException
     */
    public String buildJwtToken(Map<String, String> claims)
            throws GeneralSecurityException {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + 60 * 60 * 12 * 1000); // 12 hours
        return Jwts.builder()
                   .setIssuer(configProperties.getCgnPortalBaseUrl())
                   .setClaims(claims)
                   .setIssuedAt(issuedAt)
                   .setExpiration(expiresAt)
                   .signWith(SignatureAlgorithm.RS256, loadPrivateKey(configProperties.getJwtPrivateKey()))
                   .compact();
    }

    /**
     * Verify that token is signed correctly and returns claims
     *
     * @param token
     * @return
     * @throws GeneralSecurityException
     */
    public Claims getClaimsFromSignedToken(String token)
            throws GeneralSecurityException {
        return (Claims) Jwts.parserBuilder()
                            .build()
                            .setSigningKey(loadPublicKey(configProperties.getJwtPublicKey()))
                            .requireIssuer(configProperties.getCgnPortalBaseUrl())
                            .parse(token)
                            .getBody();
    }

    private Key loadPrivateKey(String key64)
            throws GeneralSecurityException {
        byte[] clear = Base64.getDecoder().decode(key64.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return privateKey;
    }

    private static Key loadPublicKey(String stored)
            throws GeneralSecurityException {
        byte[] data = Base64.getDecoder().decode((stored.getBytes()));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(spec);
    }

}
