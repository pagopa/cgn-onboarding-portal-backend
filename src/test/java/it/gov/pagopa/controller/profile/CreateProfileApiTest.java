package it.gov.pagopa.controller.profile;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.cgnonboardingportal.model.*;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.service.AgreementService;
import org.junit.jupiter.api.BeforeEach;
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
class CreateProfileApiTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    private String profilePath;

    @BeforeEach
    void beforeEach() {
        AgreementEntity agreement = agreementService.createAgreementIfNotExists();
        profilePath = getProfilePath(agreement.getId());
    }

    @Test
    void Create_CreateProfile_Ok() throws Exception {
        CreateProfile createProfile = createSampleCreateOnlineProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
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
    void Create_CreateIncompleteOfflineProfile_Ok() throws Exception {
        CreateProfile createProfile = createSampleCreateOfflineWithoutRequiredAddressesProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void Create_CreateIncompleteOnlineProfile_Ok() throws Exception {
        CreateProfile createProfile = createSampleCreateOnlineProfile();
        // set to null required field websiteUrl
        OnlineChannel onlineChannel = (OnlineChannel) createProfile.getSalesChannel();
        onlineChannel.setWebsiteUrl(null);

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void Create_CreateIncompleteBothProfile_Ok() throws Exception {
        CreateProfile createProfile = createSampleCreateBothWithoutRequiredWebsiteUrlProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void Create_CreateProfileMultipleTimes_InvalidRequest() throws Exception {
        CreateProfile createProfile = createSampleCreateOnlineProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().is2xxSuccessful());
        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    private CreateProfile createSampleCreateOnlineProfile() {
        CreateProfile createProfile = createSampleCreateProfile();
        OnlineChannel onlineChannel = new OnlineChannel();
        onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
        onlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        createProfile.setSalesChannel(onlineChannel);
        return createProfile;
    }

    private CreateProfile createSampleCreateOfflineWithoutRequiredAddressesProfile() {
        CreateProfile createProfile = createSampleCreateProfile();
        OfflineChannel offlineChannel = new OfflineChannel();
        offlineChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
        offlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        createProfile.setSalesChannel(offlineChannel);
        return createProfile;
    }

    private CreateProfile createSampleCreateBothWithoutRequiredWebsiteUrlProfile() {
        CreateProfile createProfile = createSampleCreateProfile();
        BothChannels bothChannels = new BothChannels();
        bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
        bothChannels.setAddresses(createSampleAddressDto());
        createProfile.setSalesChannel(bothChannels);
        return createProfile;
    }

    private CreateProfile createSampleCreateProfile() {
        CreateProfile createProfile = new CreateProfile();
        createProfile.setFullName("profile_full_name");
        createProfile.setName("profile_name");
        createProfile.setDescription("profile_description");
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

}

