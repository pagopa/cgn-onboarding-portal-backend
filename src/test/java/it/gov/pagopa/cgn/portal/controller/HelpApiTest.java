package it.gov.pagopa.cgn.portal.controller;


import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.recaptcha.GoogleRecaptchaResponse;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(HelpApiTest.IntegrationAbstractTestConfiguration.class)
class HelpApiTest extends IntegrationAbstractTest {

    @Autowired
    private RestTemplate restTemplateMock;

    @Autowired
    private MockMvc mockMvc;

    private AgreementEntity agreement;

    @TestConfiguration
    static class IntegrationAbstractTestConfiguration {

        @Bean
        @Primary
        public RestTemplate getRestTemplateBean() {
            return mock(RestTemplate.class);
        }
    }

    @BeforeEach
    void init() {
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PUBLICADMINISTRATION);
        setOperatorAuth();
    }

    @Test
    void Send_SendPublicHelpRequest_Ok() throws Exception {

        it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest helpRequest = TestUtils.createSamplePublicApiHelpRequest();

        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(true);
        when(restTemplateMock.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class))).thenReturn(response);

        this.mockMvc.perform(
                post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(helpRequest))
        )
                .andDo(log())
                .andExpect(status().isNoContent());
    }


    @Test
    void Send_SendPublicHelpRequestWithInvalidToken_BadRequest() throws Exception {

        it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest helpRequest = TestUtils.createSamplePublicApiHelpRequest();

        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(false);
        when(restTemplateMock.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class))).thenReturn(response);

        this.mockMvc.perform(
                post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(helpRequest))
        )
                .andDo(log())
                .andExpect(status().isBadRequest());
    }

    @Test
    void Send_SendAuthenticatedHelpRequest_Ok() throws Exception {

        it.gov.pagopa.cgnonboardingportal.model.HelpRequest helpRequest = TestUtils.createSampleAuthenticatedHelpRequest();

        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement);
        profileService.createProfile(profileEntity, agreement.getId());

        this.mockMvc.perform(
                post(TestUtils.getAuthenticatedHelpPath(agreement.getId()))
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(helpRequest))
        )
                .andDo(log())
                .andExpect(status().isNoContent());
    }

    @Test
    void Send_SendAuthenticatedHelpRequest_FailOnMissingProfile() throws Exception {

        it.gov.pagopa.cgnonboardingportal.model.HelpRequest helpRequest = TestUtils.createSampleAuthenticatedHelpRequest();

        this.mockMvc.perform(
                post(TestUtils.getAuthenticatedHelpPath(agreement.getId()))
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(helpRequest))
        )
                .andDo(log())
                .andExpect(status().isBadRequest());
    }
}
