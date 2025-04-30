package it.gov.pagopa.cgn.portal.controller;


import it.gov.pagopa.cgn.portal.*;
import it.gov.pagopa.cgn.portal.converter.help.*;
import it.gov.pagopa.cgn.portal.email.*;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.recaptcha.*;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.*;
import org.junit.jupiter.api.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.web.servlet.*;
import org.springframework.web.client.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PublicHelpApiControllerTest.IntegrationAbstractTestConfiguration.class)
class PublicHelpApiControllerTest
        extends IntegrationAbstractTest {

    @Autowired
    private RestTemplate restTemplateMock;

    @Autowired
    private MockMvc mockMvc;

    private AgreementEntity agreement;

    @MockBean
    private EmailNotificationFacade emailNotificationFacade;

    @MockBean
    private HelpCategoryConverter helpCategoryConverter;

    @MockBean
    private GoogleRecaptchaApi googleRecaptchaApi;


    @BeforeEach
    void init() {
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                EntityType.PRIVATE,
                TestUtils.FAKE_ORGANIZATION_NAME);
        setOperatorAuth();
    }

    @Test
    void Send_SendPublicHelpRequest_Ok() throws Exception {
        HelpRequest helpRequest = TestUtils.createSamplePublicApiHelpRequest();

        when(googleRecaptchaApi.isTokenValid(anyString())).thenReturn(true);
        when(helpCategoryConverter.helpCategoryFromEnum(any())).thenReturn("OTHER");

        this.mockMvc.perform(post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(helpRequest)))
                .andDo(log())
                .andExpect(status().isNoContent());

        verify(emailNotificationFacade).notifyDepartmentNewHelpRequest(any());
    }

    @Test
    void Send_SendPublicHelpRequestWithInvalidToken_BadRequest()
            throws Exception {

        it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest helpRequest = TestUtils.createSamplePublicApiHelpRequest();

        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(false);
        when(restTemplateMock.postForObject(anyString(),
                any(),
                eq(GoogleRecaptchaResponse.class))).thenReturn(response);

        this.mockMvc.perform(post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(helpRequest)))
                .andDo(log())
                .andExpect(status().isBadRequest());
    }

    @Test
    void Send_SendAuthenticatedHelpRequest_Ok()
            throws Exception {

        it.gov.pagopa.cgnonboardingportal.model.HelpRequest helpRequest = TestUtils.createSampleAuthenticatedHelpRequest();

        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement);
        profileService.createProfile(profileEntity, agreement.getId());

        this.mockMvc
                .perform(post(TestUtils.getAuthenticatedHelpPath(agreement.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(
                                helpRequest)))
                .andDo(log())
                .andExpect(status().isNoContent());
    }

    @Test
    void Send_SendAuthenticatedHelpRequest_FailOnMissingProfile()
            throws Exception {

        it.gov.pagopa.cgnonboardingportal.model.HelpRequest helpRequest = TestUtils.createSampleAuthenticatedHelpRequest();

        this.mockMvc
                .perform(post(TestUtils.getAuthenticatedHelpPath(agreement.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(
                                helpRequest)))
                .andDo(log())
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendHelpRequest_shouldReturn500_whenNotifyThrowsException()
            throws Exception {

        String requestJson = """
                {
                    "category": "Other",
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
        when(helpCategoryConverter.helpCategoryFromEnum(any())).thenReturn("Other");

        doThrow(new RuntimeException("Errore interno")).when(emailNotificationFacade)
                .notifyDepartmentNewHelpRequest(any());

        mockMvc.perform(post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(log())
                .andExpect(status().isInternalServerError());
    }

    @TestConfiguration
    static class IntegrationAbstractTestConfiguration {

        @Bean
        @Primary
        public RestTemplate getRestTemplateBean() {
            return mock(RestTemplate.class);
        }
    }
}
