package it.gov.pagopa.cgn.portal.controller.profile;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.model.DiscountCodeType;
import it.gov.pagopa.cgnonboardingportal.model.SalesChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class GetProfileApiTest extends IntegrationAbstractTest {

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
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PUBLICADMINISTRATION);
        profilePath = TestUtils.getProfilePath(agreement.getId());
        setOperatorAuth();
    }

    @Test
    void Get_GetProfileWithInvalidAgreementId_Forbidden() throws Exception {
        this.mockMvc.perform(
                get(TestUtils.getProfilePath("invalid")).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isForbidden());
    }

    @Test
    void Get_GetProfile_NotFound() throws Exception {
        this.mockMvc.perform(
                get(profilePath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
    }

    @Test
    void Get_GetProfile_Ok() throws Exception {
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement);
        profileEntity = profileService.createProfile(profileEntity, agreement.getId());
        this.mockMvc.perform(
                get(profilePath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.salesChannel.channelType").value(SalesChannelType.ONLINECHANNEL.getValue()))
                .andExpect(jsonPath("$.salesChannel.websiteUrl").value(profileEntity.getWebsiteUrl()))
                .andExpect(jsonPath("$.salesChannel.discountCodeType").value(DiscountCodeType.STATIC.getValue()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.fullName").value(profileEntity.getFullName()))
                .andExpect(jsonPath("$.name").value(profileEntity.getName()))
                .andExpect(jsonPath("$.taxCodeOrVat").isNotEmpty())
                .andExpect(jsonPath("$.pecAddress").value(profileEntity.getPecAddress()))
                .andExpect(jsonPath("$.description").value(profileEntity.getDescription()))
                .andExpect(jsonPath("$.legalOffice").value(profileEntity.getLegalOffice()))
                .andExpect(jsonPath("$.legalRepresentativeFullName").value(profileEntity.getLegalRepresentativeFullName()))
                .andExpect(jsonPath("$.legalRepresentativeTaxCode").value(profileEntity.getLegalRepresentativeTaxCode()))
                .andExpect(jsonPath("$.telephoneNumber").value(profileEntity.getTelephoneNumber()))
                .andExpect(jsonPath("$.referent.lastName").value(profileEntity.getReferent().getLastName()))
                .andExpect(jsonPath("$.referent.telephoneNumber").value(profileEntity.getReferent().getTelephoneNumber()))
                .andExpect(jsonPath("$.referent.emailAddress").value(profileEntity.getReferent().getEmailAddress()))
                .andExpect(jsonPath("$.referent.role").value(profileEntity.getReferent().getRole()));
    }

}

