package it.gov.pagopa.controller.profile;

import it.gov.pagopa.cgn.IntegrationAbstractTest;
import it.gov.pagopa.cgn.TestUtils;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.service.AgreementService;
import it.gov.pagopa.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GetProfileApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    private String profilePath;
    private AgreementEntity agreement;

    @AfterEach
    @BeforeEach
    void clean() {
        agreement = agreementService.createAgreementIfNotExists();
        profilePath = TestUtils.getProfilePath(agreement.getId());;
    }

    @Test
    void Get_GetProfileWithInvalidAgreementId_NotFound() throws Exception {
        this.mockMvc.perform(
                get(TestUtils.getProfilePath("invalid")).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
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
        profileEntity = profileService.createRegistry(profileEntity, agreement.getId());
        this.mockMvc.perform(
                get(profilePath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(profileEntity.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value(profileEntity.getFullName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(profileEntity.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taxCodeOrVat").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pecAddress").value(profileEntity.getPecAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(profileEntity.getDescription()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.referent").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.salesChannel").isNotEmpty());

    }

}

