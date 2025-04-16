package it.gov.pagopa.cgn.portal.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.security.JwtClaims;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.CompanyAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.model.Organization;
import it.gov.pagopa.cgnonboardingportal.model.Organizations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "mockUser", roles = {"MERCHANT"})
class ReferentOrganizationsControllerTest
        extends IntegrationAbstractTest {

    @MockBean
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void testGetOrganizations_shouldReturnOrganizations()
            throws Exception {
        CompanyAttributeAuthority company = new CompanyAttributeAuthority();
        company.setFiscalCode("12345678901");
        company.setOrganizationName(TestUtils.FAKE_ORGANIZATION_NAME);
        company.setPec("test@pec.it");

        Organizations orgs = new Organizations();
        Organization org = new Organization();
        org.setOrganizationFiscalCode("12345678901");
        org.setOrganizationName(TestUtils.FAKE_ORGANIZATION_NAME);
        org.setEmail("test@pec.it");
        org.setToken(TestUtils.FAKE_ORG_TOKEN);
        orgs.setItems(List.of(org));

        List<CompanyAttributeAuthority> companies = List.of(company);

        try (MockedStatic<CGNUtils> mockedCgnUtils = Mockito.mockStatic(CGNUtils.class)) {
            mockedCgnUtils.when(CGNUtils::getJwtOperatorFiscalCode).thenReturn(TestUtils.FAKE_FISCAL_CODE);
            mockedCgnUtils.when(CGNUtils::getJwtOperatorFirstName).thenReturn(TestUtils.FAKE_FIRST_NAME);
            mockedCgnUtils.when(CGNUtils::getJwtOperatorLastName).thenReturn(TestUtils.FAKE_LAST_NAME);

            when(attributeAuthorityService.getAgreementOrganizations(TestUtils.FAKE_FISCAL_CODE)).thenReturn(companies);

            MvcResult result = mockMvc.perform(get(TestUtils.REFERENT_ORGANIZATIONS_PATH).contentType(MediaType.APPLICATION_JSON))
                                      .andExpect(status().isOk())
                                      .andExpect(jsonPath("$.items[0].organization_name").value(TestUtils.FAKE_ORGANIZATION_NAME))
                                      .andExpect(jsonPath("$.items[0].token").isNotEmpty())
                                      .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(responseBody);
            String token = json.get("items").get(0).get("token").asText();

            String firstNameIntoJwt = jwtUtils.getClaimsFromSignedToken(token)
                                              .get(JwtClaims.FIRST_NAME.getCode(), String.class);

            Assertions.assertEquals(TestUtils.FAKE_FIRST_NAME, firstNameIntoJwt);

        }
    }
}
