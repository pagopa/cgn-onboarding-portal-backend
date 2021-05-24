package it.gov.pagopa.cgn.portal.controller.profile;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CreateProfileApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    private String profilePath;

    @BeforeEach
    void init() {
        AgreementEntity agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        profilePath = TestUtils.getProfilePath(agreement.getId());
        setOperatorAuth();
    }

    @Test
    void Create_CreateOnlineProfile_Ok() throws Exception {
        CreateProfile createProfile = createSampleCreateOnlineProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.salesChannel.channelType").value(SalesChannelType.ONLINECHANNEL.getValue()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value(createProfile.getFullName()))
                .andExpect(jsonPath("$.name").value(createProfile.getName()))
                .andExpect(jsonPath("$.taxCodeOrVat").value(createProfile.getTaxCodeOrVat()))
                .andExpect(jsonPath("$.pecAddress").value(createProfile.getPecAddress()))
                .andExpect(jsonPath("$.description").value(createProfile.getDescription()))
                .andExpect(jsonPath("$.legalOffice").value(createProfile.getLegalOffice()))
                .andExpect(jsonPath("$.legalRepresentativeFullName").value(createProfile.getLegalRepresentativeFullName()))
                .andExpect(jsonPath("$.legalRepresentativeTaxCode").value(createProfile.getLegalRepresentativeTaxCode()))
                .andExpect(jsonPath("$.telephoneNumber").value(createProfile.getTelephoneNumber()))
                .andExpect(jsonPath("$.referent.lastName").value(createProfile.getReferent().getLastName()))
                .andExpect(jsonPath("$.referent.telephoneNumber").value(createProfile.getReferent().getTelephoneNumber()))
                .andExpect(jsonPath("$.referent.emailAddress").value(createProfile.getReferent().getEmailAddress()))
                .andExpect(jsonPath("$.referent.role").value(createProfile.getReferent().getRole()));

    }

    @Test
    void Create_CreateOfflineProfile_Ok() throws Exception {
        CreateProfile createProfile = createSampleCreateOffline();
        OfflineChannel offlineChannel = (OfflineChannel) createProfile.getSalesChannel();
        Address firstAddress = offlineChannel.getAddresses().get(0);

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.salesChannel.channelType").value(SalesChannelType.OFFLINECHANNEL.getValue()))
                .andExpect(jsonPath("$.salesChannel.addresses").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel.addresses").isArray())
                .andExpect(jsonPath("$.salesChannel.addresses[0].fullAddress").value(firstAddress.getFullAddress()))
                .andExpect(jsonPath("$.salesChannel.addresses[0].coordinates").isNotEmpty())
                .andExpect(jsonPath("$.salesChannel.addresses[0].coordinates.latitude").value(firstAddress.getCoordinates().getLatitude()))
                .andExpect(jsonPath("$.salesChannel.addresses[0].coordinates.longitude").value(firstAddress.getCoordinates().getLongitude()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value(createProfile.getFullName()))
                .andExpect(jsonPath("$.name").value(createProfile.getName()))
                .andExpect(jsonPath("$.taxCodeOrVat").value(createProfile.getTaxCodeOrVat()))
                .andExpect(jsonPath("$.pecAddress").value(createProfile.getPecAddress()))
                .andExpect(jsonPath("$.description").value(createProfile.getDescription()))
                .andExpect(jsonPath("$.legalOffice").value(createProfile.getLegalOffice()))
                .andExpect(jsonPath("$.legalRepresentativeFullName").value(createProfile.getLegalRepresentativeFullName()))
                .andExpect(jsonPath("$.legalRepresentativeTaxCode").value(createProfile.getLegalRepresentativeTaxCode()))
                .andExpect(jsonPath("$.telephoneNumber").value(createProfile.getTelephoneNumber()))
                .andExpect(jsonPath("$.referent.lastName").value(createProfile.getReferent().getLastName()))
                .andExpect(jsonPath("$.referent.telephoneNumber").value(createProfile.getReferent().getTelephoneNumber()))
                .andExpect(jsonPath("$.referent.emailAddress").value(createProfile.getReferent().getEmailAddress()))
                .andExpect(jsonPath("$.referent.role").value(createProfile.getReferent().getRole()));

    }

    @Test
    void Create_CreateIncompleteOfflineProfile_BadRequest() throws Exception {
        CreateProfile createProfile = createSampleCreateOfflineWithoutRequiredAddressesProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void Create_CreateIncompleteOnlineProfile_BadRequest() throws Exception {
        CreateProfile createProfile = createSampleCreateOnlineProfile();
        // set to null required field websiteUrl
        OnlineChannel onlineChannel = (OnlineChannel) createProfile.getSalesChannel();
        onlineChannel.setWebsiteUrl(null);

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void Create_CreateIncompleteBothProfile_BadRequest() throws Exception {
        CreateProfile createProfile = createSampleCreateBothWithoutRequiredWebsiteUrlProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void Create_CreateProfileMultipleTimes_BadRequest() throws Exception {
        CreateProfile createProfile = createSampleCreateOnlineProfile();

        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().is2xxSuccessful());
        this.mockMvc.perform(
                post(profilePath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(createProfile)))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    private CreateProfile createSampleCreateOnlineProfile() {
        CreateProfile createProfile = createSampleCreateProfile();
        OnlineChannel onlineChannel = new OnlineChannel();
        onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
        onlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        onlineChannel.setDiscountCodeType(DiscountCodeType.API);
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

    private CreateProfile createSampleCreateOffline() {
        CreateProfile profile = createSampleCreateOfflineWithoutRequiredAddressesProfile();
        OfflineChannel offlineChannel = (OfflineChannel) profile.getSalesChannel();
        offlineChannel.setAddresses(TestUtils.createSampleAddressDto());
        profile.setSalesChannel(offlineChannel);
        return profile;
    }

    private CreateProfile createSampleCreateBothWithoutRequiredWebsiteUrlProfile() {
        CreateProfile createProfile = createSampleCreateProfile();
        BothChannels bothChannels = new BothChannels();
        bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
        bothChannels.setAddresses(TestUtils.createSampleAddressDto());
        createProfile.setSalesChannel(bothChannels);
        return createProfile;
    }

    private CreateProfile createSampleCreateProfile() {
        CreateProfile createProfile = new CreateProfile();
        createProfile.setFullName("profile_full_name");
        createProfile.setName("profile_name");
        createProfile.setTaxCodeOrVat("abcdeghilmnopqrs");
        createProfile.setDescription("profile_description");
        createProfile.setPecAddress("pec.address@pagopa.it");
        createProfile.setLegalOffice("legalOffice");
        createProfile.setLegalRepresentativeFullName("legal representative full name");
        createProfile.setTelephoneNumber("12345678");
        createProfile.setLegalRepresentativeTaxCode("abcdeghilmnopqrs");
        createProfile.setReferent(createSampleCreateReferent());
        return createProfile;
    }

    private CreateReferent createSampleCreateReferent() {
        CreateReferent createReferent = new CreateReferent();
        createReferent.setFirstName("first_name");
        createReferent.setLastName("last_name");
        createReferent.setEmailAddress("referent.registry@pagopa.it");
        createReferent.setTelephoneNumber("+390123456789");
        createReferent.setRole("CEO");
        return createReferent;
    }
}

