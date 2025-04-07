package it.gov.pagopa.cgn.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
     * @param claims a hashmap containing the claims to insert into JWT token
     * @return String
     */
    public String buildJwtToken(Map<String, String> claims) {
        try {
            Date issuedAt = new Date();
            Date expiresAt = new Date(issuedAt.getTime() + 60 * 60 * 12 * 1000); // 12 hours
            return Jwts.builder()
                       .setClaims(claims)
                       .setIssuer(configProperties.getCgnPortalBaseUrl())
                       .setIssuedAt(issuedAt)
                       .setExpiration(expiresAt)
                       .signWith(loadPrivateKey(configProperties.getJwtPrivateKey()), SignatureAlgorithm.RS256)
                       .compact();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Verify that token is signed correctly and returns claims
     *
     * @param token a JWT token in string format
     * @return Claims
     */
    public Claims getClaimsFromSignedToken(String token) {
        try {

            return (Claims) Jwts.parserBuilder()
                                .setSigningKey(loadPublicKey(configProperties.getJwtPublicKey()))
                                .requireIssuer(configProperties.getCgnPortalBaseUrl())
                                .build()
                                .parse(token)
                                .getBody();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public JwtUser getUserDetails(String token) {
        if (token==null) return null;
        final Claims claims = getClaimsFromSignedToken(token);
        if (isOperator(claims)) {
            String organizationFiscalCode = claims.get(JwtClaims.ORGANIZATION_FISCAL_CODE.getCode(), String.class);
            return new JwtOperatorUser(claims.get(JwtClaims.FIRST_NAME.getCode(), String.class),
                                       claims.get(JwtClaims.LAST_NAME.getCode(), String.class),
                                       claims.get(JwtClaims.FISCAL_CODE.getCode(), String.class),
                                       organizationFiscalCode);
        } else if (isAdmin(claims)) {
            return new JwtAdminUser(claims.get(JwtClaims.FIRST_NAME.getCode(), String.class) + " " +
                                    claims.get(JwtClaims.LAST_NAME.getCode(), String.class));
        } else {
            throw new SecurityException("Invalid role.");
        }
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

    private boolean isAdmin(Claims claims) {
        return CgnUserRoleEnum.ADMIN.getCode().equals(claims.get(JwtClaims.ROLE.getCode(), String.class));
    }

    private boolean isOperator(Claims claims) {
        return CgnUserRoleEnum.OPERATOR.getCode().equals(claims.get(JwtClaims.ROLE.getCode(), String.class));
    }
}
