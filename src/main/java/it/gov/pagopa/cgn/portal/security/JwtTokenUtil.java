package it.gov.pagopa.cgn.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

    static final String CLAIM_KEY_USER_TAX_CODE = "fiscal_number";
    static final String CLAIM_KEY_USER_FAMILY_NAME = "family_name";
    static final String CLAIM_KEY_USER_NAME = "name";
    static final String CLAIM_KEY_MERCHANT_TAX_CODE =  "fiscal_number"; //"merchant_tax_code"; TODO wait for SPID professional integration
    static final String CLAIM_KEY_MERCHANT_LEGAL_NAME = "family_name"; //"merchant_legal_name"; TODO wait for SPID professional integration


    public JwtUser getUserDetails(String token, String cgnRole) {

        if (token == null) {
            return null;
        }

        final Claims claims = getClaimsFromToken(token);

        if (cgnRole.equals(CgnUserRoleEnum.OPERATOR.getCode())) {

            return new JwtOperatorUser(
                    claims.get(CLAIM_KEY_USER_TAX_CODE, String.class),
                    claims.get(CLAIM_KEY_MERCHANT_TAX_CODE, String.class),
                    claims.get(CLAIM_KEY_MERCHANT_LEGAL_NAME, String.class)
            );
        } else if (cgnRole.equals(CgnUserRoleEnum.ADMIN.getCode())) {
            return new JwtAdminUser(
                    claims.get(CLAIM_KEY_USER_TAX_CODE, String.class),
                    claims.get(CLAIM_KEY_USER_NAME, String.class) + " " + claims.get(CLAIM_KEY_USER_FAMILY_NAME, String.class)
            );
        } else {
            throw new SecurityException("Invalid role value: " + cgnRole);
        }
    }

    private Claims getClaimsFromToken(String token) {
        String[] splitToken = token.split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        return (Claims) Jwts.parser().parse(unsignedToken).getBody();
    }
}
