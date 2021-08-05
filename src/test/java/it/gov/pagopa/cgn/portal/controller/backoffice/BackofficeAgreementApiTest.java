package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.BackofficeRequestSortColumnEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
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
        setAdminAuth();
    }

    @Test
    void GetAgreements_GetAgreementsPending_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity pendingAgreement = agreementTestObject.getAgreementEntity();
        List<DiscountEntity> discounts = agreementTestObject.getDiscountEntityList();
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
                .andExpect(jsonPath("$.items[0].requestDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.items[0].profile").isNotEmpty())
                .andExpect(jsonPath("$.items[0].profile.id").isNotEmpty())
                .andExpect(jsonPath("$.items[0].profile.agreementId").value(pendingAgreement.getId()))
                .andExpect(jsonPath("$.items[0].discounts[0].id").value(discountEntity.getId()))
                .andExpect(jsonPath("$.items[0].documents").isNotEmpty())
                .andExpect(jsonPath("$.items[0].documents", hasSize(2)));

    }

    @Test
    void GetAgreements_GetAssignedToMeAgreements_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithStatusFilterPath("AssignedAgreement", Optional.of("Me"))))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(1));

    }

    @Test
    void GetAgreements_GetAssignedToOtherAgreements_NotFound() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithStatusFilterPath("AssignedAgreement", Optional.of("Others"))))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void GetAgreements_GetPendingAgreement_NotFound() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithStatusFilterPath("PendingAgreement", Optional.empty())))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void GetAgreements_GetPendingAgreementSortedByOperator_Ok() throws Exception {
        List<AgreementTestObject> testObjectList = createMultiplePendingAgreement(5);
        List<AgreementEntity> sortedByOperatorAgreementList = testObjectList.stream()
                .sorted(Comparator.comparing(a -> a.getProfileEntity().getFullName()))
                .map(AgreementTestObject::getAgreementEntity)
                .collect(Collectors.toList());
        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.OPERATOR, Sort.Direction.ASC)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[0].id").value(sortedByOperatorAgreementList.get(0).getId()))
                .andExpect(jsonPath("$.items[1].id").value(sortedByOperatorAgreementList.get(1).getId()))
                .andExpect(jsonPath("$.items[2].id").value(sortedByOperatorAgreementList.get(2).getId()))
                .andExpect(jsonPath("$.items[3].id").value(sortedByOperatorAgreementList.get(3).getId()))
                .andExpect(jsonPath("$.items[4].id").value(sortedByOperatorAgreementList.get(4).getId()));

    }

    @Test
    void GetAgreements_GetPendingAgreementSortedByRequestDate_Ok() throws Exception {
        List<AgreementTestObject> testObjectList = createMultiplePendingAgreement(5);
        List<AgreementEntity> sortedByRequestDateAgreementList = testObjectList.stream()
                .map(AgreementTestObject::getAgreementEntity)
                .sorted(Comparator.comparing(AgreementEntity::getRequestApprovalTime).reversed())
                .collect(Collectors.toList());

        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.REQUEST_DATE, Sort.Direction.DESC)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[0].id").value(sortedByRequestDateAgreementList.get(0).getId()))
                .andExpect(jsonPath("$.items[1].id").value(sortedByRequestDateAgreementList.get(1).getId()))
                .andExpect(jsonPath("$.items[2].id").value(sortedByRequestDateAgreementList.get(2).getId()))
                .andExpect(jsonPath("$.items[3].id").value(sortedByRequestDateAgreementList.get(3).getId()))
                .andExpect(jsonPath("$.items[4].id").value(sortedByRequestDateAgreementList.get(4).getId()));

    }

    @Test
    void GetAgreements_GetPendingAgreementSortedByAssignee_Ok() throws Exception {
        List<AgreementTestObject> testObjectList = createMultiplePendingAgreement(5);
        List<AgreementEntity> agreementEntityList = testObjectList.stream()
                .map(AgreementTestObject::getAgreementEntity).collect(Collectors.toList());
        Assertions.assertEquals(5, agreementEntityList.size());
        backofficeAgreementService.assignAgreement(agreementEntityList.get(2).getId());

        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.ASSIGNEE, Sort.Direction.ASC)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[0].id").value(agreementEntityList.get(2).getId()))
                .andExpect(jsonPath("$.items[0].state").value(AgreementState.ASSIGNEDAGREEMENT.getValue()))
                .andExpect(jsonPath("$.items[0].assignee").isNotEmpty())
                .andExpect(jsonPath("$.items[1].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[1].state").value(AgreementState.PENDINGAGREEMENT.getValue()))
                .andExpect(jsonPath("$.items[2].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[2].state").value(AgreementState.PENDINGAGREEMENT.getValue()))
                .andExpect(jsonPath("$.items[3].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[3].state").value(AgreementState.PENDINGAGREEMENT.getValue()))
                .andExpect(jsonPath("$.items[4].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[4].state").value(AgreementState.PENDINGAGREEMENT.getValue()));

    }

    @Test
    void GetAgreements_GetPendingAgreementSortedByState_Ok() throws Exception {
        List<AgreementTestObject> testObjectList = createMultiplePendingAgreement(5);
        List<AgreementEntity> agreementEntityList = testObjectList.stream()
                .map(AgreementTestObject::getAgreementEntity).collect(Collectors.toList());
        Assertions.assertEquals(5, agreementEntityList.size());
        AgreementEntity assignedAgreement = agreementEntityList.get(2);
        assignedAgreement = backofficeAgreementService.assignAgreement(assignedAgreement.getId());


        this.mockMvc.perform(
                get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.STATE, Sort.Direction.ASC)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[0].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[1].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[2].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[3].assignee").doesNotExist())
                .andExpect(jsonPath("$.items[4].id").value(assignedAgreement.getId()))
                .andExpect(jsonPath("$.items[4].assignee.fullName").value(assignedAgreement.getBackofficeAssignee()));

    }

    @Test
    void DeleteDocument_DeleteDocument_Ok() throws Exception {
        String documentTypeDto = "AdhesionRequest";
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        DocumentEntity document = TestUtils.createDocument(
                pendingAgreement, DocumentTypeEnum.BACKOFFICE_ADHESION_REQUEST);
        documentRepository.save(document);
        this.mockMvc.perform(
                delete(TestUtils.getBackofficeDocumentPath(pendingAgreement.getId()) + "/" + documentTypeDto))
                .andDo(log())
                .andExpect(status().isNoContent());

    }

    @Test
    void DeleteDocument_DeleteDocumentNotFound_BadRequest() throws Exception {
        String documentTypeDto = "AdhesionRequest";
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
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
