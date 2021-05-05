package it.gov.pagopa.cgn.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

    Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);


    static final String CLAIM_KEY_USER_TAX_CODE = "fiscal_number";
    static final String CLAIM_KEY_USER_FAMILY_NAME = "family_name";
    static final String CLAIM_KEY_USER_NAME = "name";
    static final String CLAIM_KEY_MERCHANT_TAX_CODE =  "fiscal_number"; //"merchant_tax_code"; TODO wait for SPID professional integration
    static final String CLAIM_KEY_MERCHANT_LEGAL_NAME = "family_name"; //"merchant_legal_name"; TODO wait for SPID professional integration

    @Autowired
    ObjectMapper objectMapper;

    public JwtUser getUserDetails(String token, String cgnRole) {

        if(token == null){
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
            logger.error("Invalid role value: " + cgnRole);
            return null;
        }
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            String[] splitToken = token.split("\\.");
            String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

            claims = (Claims) Jwts.parser()
                    .parse(unsignedToken)
                    .getBody();

        } catch (Exception e) {
            e.printStackTrace();
            claims = null;
        }
        return claims;
    }
}
