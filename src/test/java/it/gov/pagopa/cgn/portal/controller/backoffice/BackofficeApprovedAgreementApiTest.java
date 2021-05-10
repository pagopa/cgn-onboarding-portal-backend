package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeApprovedAgreementApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
    }

    @Test
    void GetAgreements_GetAgreementsApproved_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createApprovedAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        this.mockMvc.perform(
                get(TestUtils.AGREEMENT_APPROVED_CONTROLLER_PATH))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].agreementId").value(agreementEntity.getId()))
                .andExpect(jsonPath("$.items[0].fullName")
                        .value(agreementTestObject.getProfileEntity().getFullName()))
                .andExpect(jsonPath("$.items[0].agreementStartDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.items[0].agreementLastUpdateDate").value(LocalDate.now().toString()));

    }


    @Test
    void GetAgreements_GetAgreementsApprovedDetails_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createApprovedAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        ProfileEntity profileEntity = agreementTestObject.getProfileEntity();
        DiscountEntity discountEntity = agreementTestObject.getDiscountEntityList().get(0);
        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        this.mockMvc.perform(
                get(TestUtils.AGREEMENT_APPROVED_CONTROLLER_PATH + agreementEntity.getId()))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("agreementId").value(agreementEntity.getId()))
                .andExpect(jsonPath("profile").isNotEmpty())
                .andExpect(jsonPath("profile.name").value(profileEntity.getName()))
                .andExpect(jsonPath("profile.description").value(profileEntity.getDescription()))
                .andExpect(jsonPath("profile.imageUrl").value(agreementEntity.getImageUrl()))
                .andExpect(jsonPath("profile.fullName").value(profileEntity.getFullName()))
                .andExpect(jsonPath("profile.taxCodeOrVat").value(profileEntity.getTaxCodeOrVat()))
                .andExpect(jsonPath("profile.pecAddress").value(profileEntity.getPecAddress()))
                .andExpect(jsonPath("profile.legalOffice").value(profileEntity.getLegalOffice()))
                .andExpect(jsonPath("profile.telephoneNumber").value(profileEntity.getTelephoneNumber()))
                .andExpect(jsonPath("profile.legalRepresentativeFullName")
                        .value(profileEntity.getLegalRepresentativeFullName()))
                .andExpect(jsonPath("profile.legalRepresentativeTaxCode")
                        .value(profileEntity.getLegalRepresentativeTaxCode()))
                .andExpect(jsonPath("profile.referent").isNotEmpty())
                .andExpect(jsonPath("discounts[0].id").value(discountEntity.getId()))
                .andExpect(jsonPath("discounts[0].name").value(discountEntity.getName()));

    }

}
