package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.help.HelpCategoryConverter;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.recaptcha.GoogleRecaptchaApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class PublicHelpApiControllerTest
        extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailNotificationFacade emailNotificationFacade;

    @MockBean
    private HelpCategoryConverter helpCategoryConverter;

    @MockBean
    private GoogleRecaptchaApi googleRecaptchaApi;

    @Test
    void sendHelpRequest_shouldReturn500_whenNotifyThrowsException()
            throws Exception {

        String requestJson = """
                {
                    "category": "OTHER",
                    "topic": "Problema tecnico",
                    "message": "Ho un problema con la piattaforma.",
                    "emailAddress": "utente@example.com",
                    "referentFirstName": "Mario",
                    "referentLastName": "Rossi",
                    "legalName": "Azienda Srl",
                    "recaptchaToken": "valid-token"
                }
                """;

        when(googleRecaptchaApi.isTokenValid("valid-token")).thenReturn(true);
        when(helpCategoryConverter.helpCategoryFromEnum(any())).thenReturn("OTHER");

        doThrow(new RuntimeException("Errore interno")).when(emailNotificationFacade)
                                                       .notifyDepartmentNewHelpRequest(any());

        mockMvc.perform(post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH).contentType(MediaType.APPLICATION_JSON)
                                                                   .content(requestJson))
               .andExpect(status().isInternalServerError());
    }
}

