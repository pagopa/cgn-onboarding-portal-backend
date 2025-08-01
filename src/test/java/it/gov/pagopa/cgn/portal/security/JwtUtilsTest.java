package it.gov.pagopa.cgn.portal.security;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;


@RunWith(SpringRunner.class)
@TestPropertySource(locations = {"classpath:application.properties"})
@ContextConfiguration(classes = {ConfigProperties.class, JwtUtils.class})
public class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void GetUserDetails_InvalidRole_ThrowSecurityException() {
        String token = createOperatorSessionToken("invalid_role");
        Assert.assertThrows(SecurityException.class, () -> jwtUtils.getUserDetails(token));
    }

    @Test
    public void buildJwtToken_shouldThrowSecurityException_whenSigningFails() {
        assertThrows(SecurityException.class, () -> jwtUtils.buildJwtToken(null));
    }

    @Test
    public void GetUserDetails_TokenNull_ThrowSecurityException() {
        Assert.assertNull(jwtUtils.getUserDetails(null));
    }

    @Test
    public void GetUserDetails_OperatorSessionClaims_Ok() {
        String token = createOperatorSessionToken(CgnUserRoleEnum.OPERATOR.getCode());
        JwtUser jwtUser = jwtUtils.getUserDetails(token);
        Assert.assertTrue(jwtUser instanceof JwtOperatorUser);
        JwtOperatorUser operator = (JwtOperatorUser) jwtUser;
        Assert.assertEquals(new SimpleGrantedAuthority(CgnUserRoleEnum.OPERATOR.getCode()), operator.getAuthority());
        Assert.assertEquals(TestUtils.FAKE_FISCAL_CODE, operator.getUserTaxCode());
        Assert.assertEquals(TestUtils.FAKE_FIRST_NAME, operator.getUserFirstName());
        Assert.assertEquals(TestUtils.FAKE_LAST_NAME, operator.getUserLastName());
        Assert.assertNull(operator.getCompanyTaxCode());
    }

    @Test
    public void GetUserDetails_OperatorOrganizationClaims_Ok() {
        String token = createOperatorOrganizationToken();
        JwtUser jwtUser = jwtUtils.getUserDetails(token);
        Assert.assertTrue(jwtUser instanceof JwtOperatorUser);
        JwtOperatorUser operator = (JwtOperatorUser) jwtUser;
        Assert.assertEquals(new SimpleGrantedAuthority(CgnUserRoleEnum.OPERATOR.getCode()), operator.getAuthority());
        Assert.assertEquals(TestUtils.FAKE_FISCAL_CODE, operator.getUserTaxCode());
        Assert.assertEquals(TestUtils.FAKE_FIRST_NAME, operator.getUserFirstName());
        Assert.assertEquals(TestUtils.FAKE_LAST_NAME, operator.getUserLastName());
        Assert.assertEquals(TestUtils.FAKE_ORGANIZATION_FISCAL_CODE, operator.getCompanyTaxCode());
    }

    @Test
    public void GetUserDetails_AdminClaims_Ok() {
        String token = createAdminToken();
        JwtUser jwtUser = jwtUtils.getUserDetails(token);
        Assert.assertTrue(jwtUser instanceof JwtAdminUser);
        JwtAdminUser admin = (JwtAdminUser) jwtUser;
        Assert.assertEquals(new SimpleGrantedAuthority(CgnUserRoleEnum.ADMIN.getCode()), admin.getAuthority());
        Assert.assertEquals(TestUtils.FAKE_FIRST_NAME + " " + TestUtils.FAKE_LAST_NAME, admin.getUserFullName());
    }


    private String createOperatorSessionToken(String role) {
        Map<String, String> claims = new HashMap<>();
        claims.put(JwtClaims.FIRST_NAME.getCode(), TestUtils.FAKE_FIRST_NAME);
        claims.put(JwtClaims.LAST_NAME.getCode(), TestUtils.FAKE_LAST_NAME);
        claims.put(JwtClaims.FISCAL_CODE.getCode(), TestUtils.FAKE_FISCAL_CODE);
        claims.put(JwtClaims.ROLE.getCode(), role);
        String token = jwtUtils.buildJwtToken(claims);
        Assert.assertNotNull(token);
        return token;
    }

    private String createOperatorOrganizationToken() {
        Map<String, String> claims = new HashMap<>();
        claims.put(JwtClaims.FIRST_NAME.getCode(), TestUtils.FAKE_FIRST_NAME);
        claims.put(JwtClaims.LAST_NAME.getCode(), TestUtils.FAKE_LAST_NAME);
        claims.put(JwtClaims.FISCAL_CODE.getCode(), TestUtils.FAKE_FISCAL_CODE);
        claims.put(JwtClaims.ORGANIZATION_FISCAL_CODE.getCode(), TestUtils.FAKE_ORGANIZATION_FISCAL_CODE);
        claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.OPERATOR.getCode());
        String token = jwtUtils.buildJwtToken(claims);
        Assert.assertNotNull(token);
        return token;
    }

    private String createAdminToken() {
        Map<String, String> claims = new HashMap<>();
        claims.put(JwtClaims.FIRST_NAME.getCode(), TestUtils.FAKE_FIRST_NAME);
        claims.put(JwtClaims.LAST_NAME.getCode(), TestUtils.FAKE_LAST_NAME);
        claims.put(JwtClaims.FISCAL_CODE.getCode(), TestUtils.FAKE_FISCAL_CODE);
        claims.put(JwtClaims.ORGANIZATION_FISCAL_CODE.getCode(), TestUtils.FAKE_ORGANIZATION_FISCAL_CODE);
        claims.put(JwtClaims.ROLE.getCode(), CgnUserRoleEnum.ADMIN.getCode());
        String token = jwtUtils.buildJwtToken(claims);
        Assert.assertNotNull(token);
        return token;
    }
}
