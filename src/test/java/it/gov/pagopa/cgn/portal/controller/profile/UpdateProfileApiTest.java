package it.gov.pagopa.cgn.portal.controller.profile;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgnonboardingportal.model.BothChannels;
import it.gov.pagopa.cgnonboardingportal.model.OfflineChannel;
import it.gov.pagopa.cgnonboardingportal.model.SalesChannelType;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateProfileApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    private String profilePath;
    private AgreementEntity agreement;

    @BeforeEach
    void init() {
        agreement = agreementService.createAgreementIfNotExists();
        profilePath = TestUtils.getProfilePath(agreement.getId());
    }

    @Test
    void Update_UpdateProfileWithInvalidAgreementId_NotFound() throws Exception {
        this.mockMvc.perform(
                get(TestUtils.getProfilePath("invalid")).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
    }

    @Test
    void Update_UpdateProfileNotExists_NotFound() throws Exception {
        this.mockMvc.perform(
                get(profilePath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
    }

    @Test
    void Update_UpdateOnlineProfileToOffline_Ok() throws Exception {
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement);
        profileEntity = profileService.createRegistry(profileEntity, agreement.getId());
        UpdateProfile updateProfile = TestUtils.createSampleUpdateProfileWithCommonFields();
        OfflineChannel offlineChannel = new OfflineChannel();
        offlineChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
        offlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        offlineChannel.setAddresses(TestUtils.createSampleAddressDto());
        updateProfile.setSalesChannel(offlineChannel);

        this.mockMvc.perform(
                put(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(updateProfile)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(profileEntity.getId()))
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.fullName").value(profileEntity.getFullName()))
                .andExpect(jsonPath("$.name").value(updateProfile.getName()))
                .andExpect(jsonPath("$.taxCodeOrVat").isNotEmpty())
                .andExpect(jsonPath("$.pecAddress").value(updateProfile.getPecAddress()))
                .andExpect(jsonPath("$.description").value(updateProfile.getDescription()))
                .andExpect(jsonPath("$.referent").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel.channelType").value(offlineChannel.getChannelType().getValue()))
                .andExpect(jsonPath("$.salesChannel.addresses").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel.websiteUrl").value(offlineChannel.getWebsiteUrl()));

    }

    @Test
    void Update_UpdateOnlineProfileToBoth_Ok() throws Exception {
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement);
        profileEntity = profileService.createRegistry(profileEntity, agreement.getId());
        UpdateProfile updateProfile = TestUtils.createSampleUpdateProfileWithCommonFields();
        BothChannels bothChannels = new BothChannels();
        bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
        bothChannels.setWebsiteUrl("https://www.pagopa.gov.it/");
        bothChannels.setAddresses(TestUtils.createSampleAddressDto());
        updateProfile.setSalesChannel(bothChannels);

        this.mockMvc.perform(
                put(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(updateProfile)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(profileEntity.getId()))
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.fullName").value(profileEntity.getFullName()))
                .andExpect(jsonPath("$.name").value(updateProfile.getName()))
                .andExpect(jsonPath("$.taxCodeOrVat").isNotEmpty())
                .andExpect(jsonPath("$.pecAddress").value(updateProfile.getPecAddress()))
                .andExpect(jsonPath("$.description").value(updateProfile.getDescription()))
                .andExpect(jsonPath("$.referent").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel.channelType").value(bothChannels.getChannelType().getValue()))
                .andExpect(jsonPath("$.salesChannel.addresses").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel.websiteUrl").value(bothChannels.getWebsiteUrl()));

    }

}

