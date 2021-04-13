package it.gov.pagopa.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgnonboardingportal.model.*;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.service.AgreementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgreementControllerTest {
    private static final String AGREEMENTS_CONTROLLER_PATH = "/agreements/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Test
    void Create_CreateSubscription_Ok() throws Exception {
        this.mockMvc.perform(
                post(AGREEMENTS_CONTROLLER_PATH))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value(AgreementState.DRAFTAGREEMENT.getValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.profileLastModifiedDate").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.discountsLastModifiedDate").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.documentsLastModifiedDate").isEmpty());

    }

    @Test
    void Create_CreateProfile_Ok() throws Exception {
        AgreementEntity agreement = agreementService.createAgreementIfNotExists();
        final String createProfilePath = AGREEMENTS_CONTROLLER_PATH +  agreement.getId() + "/profile";
        CreateProfile createProfile = createSampleCreateProfile();

        this.mockMvc.perform(
                post(createProfilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salesChannel.channelType").value(SalesChannelType.ONLINECHANNEL.getValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value(createProfile.getFullName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(createProfile.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taxCodeOrVat").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pecAddress").value(createProfile.getPecAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(createProfile.getDescription()));

    }

    @Test
    void Create_CreateProfileMultipleTimes_InvalidRequest() throws Exception {
        AgreementEntity agreement = agreementService.createAgreementIfNotExists();
        final String createProfilePath = AGREEMENTS_CONTROLLER_PATH +  agreement.getId() + "/profile";
        CreateProfile createProfile = createSampleCreateProfile();

        this.mockMvc.perform(
                post(createProfilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().is2xxSuccessful());
        this.mockMvc.perform(
                post(createProfilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    private CreateProfile createSampleCreateProfile() {
        CreateProfile createProfile = new CreateProfile();
        createProfile.setFullName("profile_full_name");
        createProfile.setName("profile_name");
        createProfile.setDescription("profile_description");
        OnlineChannel onlineChannel = new OnlineChannel();
        onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
        onlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        createProfile.setSalesChannel(onlineChannel);
        createProfile.setPecAddress("pec.address@pagopa.it");
        createProfile.setReferent(createSampleCreateReferent());
        return createProfile;
    }

    private CreateReferent createSampleCreateReferent() {
        CreateReferent createReferent = new CreateReferent();
        createReferent.setFirstName("first_name");
        createReferent.setLastName("last_name");
        createReferent.setEmailAddress("referent.registry@pagopa.it");
        createReferent.setTelephoneNumber("+390123456789");
        return createReferent;
    }

    private String getJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}

