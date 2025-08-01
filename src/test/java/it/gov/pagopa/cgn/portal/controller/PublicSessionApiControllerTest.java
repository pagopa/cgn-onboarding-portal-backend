package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.facade.SessionFacade;
import it.gov.pagopa.cgn.portal.security.OneIdentityUser;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.ActiveDirectoryData;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.CreateJwtSessionTokenRequest;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.OneIdentityData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PublicSessionApiControllerTest
        extends IntegrationAbstractTest {

    @MockBean
    private SessionFacade sessionFacade;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldThrowInternalErrorExceptionWhenFacade_ThrowsException()
            throws Exception {
        CreateJwtSessionTokenRequest request = new OneIdentityData("fake_oi_code", "1234", "oi");

        // Simula l'eccezione
        when(sessionFacade.getOperatorToken("fake_oi_code",
                                            "1234")).thenThrow(new RuntimeException("Simulated failure"));

        this.mockMvc.perform(post(TestUtils.SESSION_PATH).contentType(MediaType.APPLICATION_JSON)
                                                         .content(TestUtils.getJson(request)))
                    .andDo(log())
                    .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnRequestTypeSessionTokenWhenRequestIs_ok()
            throws Exception {
        CreateJwtSessionTokenRequest request = new OneIdentityData("fake_oi_code", "1234", "oi");

        when(sessionFacade.getOperatorToken("fake_oi_code", "1234")).thenReturn(TestUtils.FAKE_SESSION_OI_TOKEN);

        this.mockMvc.perform(post(TestUtils.SESSION_PATH).contentType(MediaType.APPLICATION_JSON)
                                                         .content(TestUtils.getJson(request)))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(TestUtils.FAKE_SESSION_OI_TOKEN));

        request = new ActiveDirectoryData("fake_ad_code", "1235", "ad");

        when(sessionFacade.getAdminToken("fake_ad_code", "1235")).thenReturn(TestUtils.FAKE_SESSION_AD_TOKEN);

        this.mockMvc.perform(post(TestUtils.SESSION_PATH).contentType(MediaType.APPLICATION_JSON)
                                                         .content(TestUtils.getJson(request)))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(TestUtils.FAKE_SESSION_AD_TOKEN));
    }

    @Test
    void shouldCreateUserAndReturnFields() {
        String fiscalCode = "RSSMRA80A01H501U";
        String firstName = "Mario";
        String lastName = "Rossi";

        OneIdentityUser user = new OneIdentityUser(fiscalCode, firstName, lastName);

        assertEquals(fiscalCode, user.getFiscalCode());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
    }
}
