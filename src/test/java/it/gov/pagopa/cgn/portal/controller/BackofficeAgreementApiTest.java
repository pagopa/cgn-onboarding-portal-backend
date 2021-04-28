package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BackofficeAgreementApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DiscountService discountService;

    private AgreementEntity pendingAgreement;

    private DiscountEntity discountEntity;

    @Test
    void GetAgreements_GetAgreementsPending_Ok() throws Exception {
        createPendingAgreement();
        this.mockMvc.perform(
                get(TestUtils.BACKOFFICE_CONTROLLER_PATH + "agreements"))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(pendingAgreement.getId()))
                .andExpect(jsonPath("$.items[0].state").value(AgreementState.PENDINGAGREEMENT.getValue()))
                .andExpect(jsonPath("$.items[0].profile").isNotEmpty())
                .andExpect(jsonPath("$.items[0].profile.id").isNotEmpty())
                .andExpect(jsonPath("$.items[0].profile.agreementId").value(pendingAgreement.getId()))
                .andExpect(jsonPath("$.items[0].discounts[0].id").value(discountEntity.getId()))
                .andExpect(jsonPath("$.items[0].documents").isNotEmpty())
                .andExpect(jsonPath("$.items[0].documents", hasSize(2)));

    }

    private void createPendingAgreement() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        pendingAgreement = agreementService.requestApproval(agreementEntity.getId());
    }

}
