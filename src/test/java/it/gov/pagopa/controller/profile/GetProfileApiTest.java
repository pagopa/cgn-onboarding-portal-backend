package it.gov.pagopa.controller.profile;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.service.AgreementService;
import it.gov.pagopa.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetProfileApiTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    private String createProfilePath;
    private AgreementEntity agreement;

    @BeforeEach
    void beforeEach() {
        agreement = agreementService.createAgreementIfNotExists();
        createProfilePath = getProfilePath(agreement.getId());;
    }

    @Test
    void Get_GetProfileWithInvalidAgreementId_NotFound() throws Exception {
        this.mockMvc.perform(
                get(getProfilePath("invalid")).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
    }

    @Test
    void Get_GetProfile_NotFound() throws Exception {
        this.mockMvc.perform(
                get(createProfilePath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
    }

    @Test
    void Get_GetProfile_Ok() throws Exception {
        ProfileEntity profileEntity = createSampleProfileEntity(agreement);
        profileEntity = profileService.createRegistry(profileEntity, agreement.getId());
        this.mockMvc.perform(
                get(createProfilePath).contentType(MediaType.APPLICATION_JSON))
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

