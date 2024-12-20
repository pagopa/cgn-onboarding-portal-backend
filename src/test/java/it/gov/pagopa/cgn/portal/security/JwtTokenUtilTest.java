package it.gov.pagopa.cgn.portal.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RunWith(SpringRunner.class)
public class JwtTokenUtilTest {

    private static final String CLAIM_KEY_MERCHANT_USER_TAX_CODE = "fiscal_number";
    private static final String CLAIM_KEY_MERCHANT_USER_TAX_CODE_EXAMPLE = "merchant_user_tax_code";
    private static final String CLAIM_KEY_MERCHANT_COMPANY_CODE = "company";
    private static final String CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE = "organization_fiscal_code";
    private static final String CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE_EXAMPLE = "merchant_user_tax_code";

    private static final String CLAIM_KEY_ADMIN_FAMILY_NAME = "family_name";
    private static final String CLAIM_KEY_ADMIN_NAME = "given_name";

    private static final String CLAIM_KEY_ADMIN_FAMILY_NAME_EXAMPLE = "family_name";
    private static final String CLAIM_KEY_ADMIN_NAME_EXAMPLE = "name";


    @Test
    public void GetUserDetails_InvalidRole_ThrowSecurityException() {
        String token = createOperatorToken();
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        Assert.assertThrows(SecurityException.class, () -> jwtTokenUtil.getUserDetails(token, "Invalid"));

    }

    @Test
    public void GetUserDetails_TokenNull_ThrowSecurityException() {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        Assert.assertNull(jwtTokenUtil.getUserDetails(null, "Invalid"));

    }

    @Test
    public void GetUserDetails_OperatorClaims_Ok() {
        String token = createOperatorToken();
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        JwtUser jwtUser = jwtTokenUtil.getUserDetails(token, CgnUserRoleEnum.OPERATOR.getCode());
        Assert.assertTrue(jwtUser instanceof JwtOperatorUser);
        JwtOperatorUser operator = (JwtOperatorUser) jwtUser;
        Assert.assertEquals(CLAIM_KEY_MERCHANT_USER_TAX_CODE_EXAMPLE, operator.getUserTaxCode());
        Assert.assertEquals(CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE_EXAMPLE, operator.getCompanyTaxCode());
    }

    @Test
    public void GetUserDetails_AdminClaims_Ok() {
        String token = createAdminToken();
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        JwtUser jwtUser = jwtTokenUtil.getUserDetails(token, CgnUserRoleEnum.ADMIN.getCode());
        Assert.assertTrue(jwtUser instanceof JwtAdminUser);
        JwtAdminUser adminUser = (JwtAdminUser) jwtUser;
        Assert.assertEquals(CLAIM_KEY_ADMIN_NAME_EXAMPLE + " " + CLAIM_KEY_ADMIN_FAMILY_NAME_EXAMPLE,
                            adminUser.getUserFullName());

    }


    private String createOperatorToken() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(CLAIM_KEY_MERCHANT_USER_TAX_CODE, CLAIM_KEY_MERCHANT_USER_TAX_CODE_EXAMPLE);
        Map<String, String> companyData = new HashMap<>();
        companyData.put(CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE, CLAIM_KEY_COMPANY_MERCHANT_TAX_CODE_EXAMPLE);
        claimsMap.put(CLAIM_KEY_MERCHANT_COMPANY_CODE, companyData);
        String token = createToken(claimsMap);
        Assert.assertNotNull(token);
        return token;
    }

    private String createAdminToken() {

        Map<String, Object> map = new HashMap<>();
        map.put(CLAIM_KEY_ADMIN_NAME, CLAIM_KEY_ADMIN_NAME_EXAMPLE);
        map.put(CLAIM_KEY_ADMIN_FAMILY_NAME, CLAIM_KEY_ADMIN_FAMILY_NAME_EXAMPLE);
        String token = createToken(map);
        Assert.assertNotNull(token);
        return token;
    }

    private String createToken(Map<String, Object> claimsMap) {
        //Let's set the JWT Claims
        String SECRET_KEY = "oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        JwtBuilder builder = Jwts.builder()
                                 .setId("id")
                                 .setIssuedAt(new Date())
                                 .setSubject("subject")
                                 .setIssuer("issuer")
                                 .setExpiration(java.util.Date.from(LocalDateTime.now()
                                                                                 .plusMonths(1)
                                                                                 .atZone(ZoneId.systemDefault())
                                                                                 .toInstant()))
                                 .setClaims(claimsMap)
                                 .signWith(signatureAlgorithm, signingKey);
        return builder.compact();
    }
}
