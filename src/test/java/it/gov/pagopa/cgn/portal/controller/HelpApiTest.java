package it.gov.pagopa.cgn.portal.controller;


import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class HelpApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    private AgreementEntity agreement;

    @BeforeEach
    void init() {
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        setOperatorAuth();
    }

    @Test
    void Send_SendPublicHelpRequest_Ok() throws Exception {

        it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest helpRequest = TestUtils.createSamplePublicApiHelpRequest();

        this.mockMvc.perform(
                post(TestUtils.PUBLIC_HELP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(helpRequest))
        )
                .andDo(log())
                .andExpect(status().isNoContent());
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
