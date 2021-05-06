package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeAgreementApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
     SecurityContextHolder.getContext().setAuthentication(
             new JwtAuthenticationToken(new JwtAdminUser(TestUtils.FAKE_ID, "admin_name"))
            );
}

    @Test
    void GetAgreements_GetAgreementsPending_Ok() throws Exception {
        AgreementEntity pendingAgreement = createPendingAgreement();
        List<DiscountEntity> discounts = discountService.getDiscounts(pendingAgreement.getId());
        DiscountEntity discountEntity = discounts.get(0);
        this.mockMvc.perform(
                get(TestUtils.AGREEMENT_REQUESTS_CONTROLLER_PATH))
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

    @Test
    void DeleteDocument_DeleteDocument_Ok() throws Exception {
        String documentTypeDto = "ManifestationOfInterest";
        AgreementEntity pendingAgreement = createPendingAgreement();
        DocumentEntity document = TestUtils.createDocument(
                pendingAgreement, DocumentTypeEnum.BACKOFFICE_MANIFESTATION_OF_INTEREST);
        documentRepository.save(document);
        this.mockMvc.perform(
                delete(TestUtils.getBackofficeDocumentPath(pendingAgreement.getId()) + "/" + documentTypeDto))
                .andDo(log())
                .andExpect(status().isNoContent());

    }

    @Test
    void DeleteDocument_DeleteDocumentNotFound_BadRequest() throws Exception {
        String documentTypeDto = "ManifestationOfInterest";
        AgreementEntity pendingAgreement = createPendingAgreement();
        this.mockMvc.perform(
                delete(TestUtils.getBackofficeDocumentPath(pendingAgreement.getId()) + "/" + documentTypeDto))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void DeleteDocument_DeleteDocumentWithWrongType_BadRequest() throws Exception {
        String documentTypeDto = "Invalid";
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        this.mockMvc.perform(
                delete(TestUtils.getBackofficeDocumentPath(agreementEntity.getId()) + "/" + documentTypeDto))
                .andDo(log())
                .andExpect(status().isBadRequest());

    }

    @Test
    void GetDocuments_GetDocuments_Ok() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        List<DocumentEntity> documents = TestUtils.createSampleBackofficeDocumentList(agreementEntity);
        documentRepository.saveAll(documents);

        this.mockMvc.perform(
                get(TestUtils.getBackofficeDocumentPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.[0].documentUrl").isNotEmpty())
                .andExpect(jsonPath("$.[0].creationDate").value(LocalDate.now().toString()));
    }

    @Test
    void GetDocuments_GetDocumentNotFound_Ok() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);

        this.mockMvc.perform(
                get(TestUtils.getBackofficeDocumentPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(0)));
    }

}
