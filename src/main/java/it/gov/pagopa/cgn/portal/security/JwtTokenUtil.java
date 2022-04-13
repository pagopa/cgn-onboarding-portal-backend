package it.gov.pagopa.cgn.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtTokenUtil {

    //merchant
    static final String CLAIM_KEY_MERCHANT_USER_TAX_CODE = "fiscal_number";
    static final String CLAIM_KEY_MERCHANT_COMPANY_CODE = "company";
    static final String CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE = "organization_fiscal_code";
    //admin
    static final String CLAIM_KEY_ADMIN_FAMILY_NAME = "family_name";
    static final String CLAIM_KEY_ADMIN_NAME = "given_name";

    public JwtUser getUserDetails(String token, String cgnRole) {

        if (token == null) {
            return null;
        }

        final Claims claims = getClaimsFromToken(token);

        if (cgnRole.equals(CgnUserRoleEnum.OPERATOR.getCode())) {
            Map<String, String> companyMap = claims.get(CLAIM_KEY_MERCHANT_COMPANY_CODE, Map.class);
            final String companyTaxCode = companyMap.get(CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE);
            return new JwtOperatorUser(claims.get(CLAIM_KEY_MERCHANT_USER_TAX_CODE, String.class), companyTaxCode);
        } else if (cgnRole.equals(CgnUserRoleEnum.ADMIN.getCode())) {
            return new JwtAdminUser(claims.get(CLAIM_KEY_ADMIN_NAME, String.class) +
                                    " " +
                                    claims.get(CLAIM_KEY_ADMIN_FAMILY_NAME, String.class));
        } else {
            throw new SecurityException("Invalid role value: " + cgnRole);
        }
    }

    // we suppress "JWT should be signed and verified with strong cipher algorithms"
    // because JWT signature is verified by APIM and portal does not own the signing key
    @SuppressWarnings("java:S5659")
    private Claims getClaimsFromToken(String token) {
        String[] splitToken = token.split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        return (Claims) Jwts.parserBuilder().build().parse(unsignedToken).getBody();
    }
}
