package it.gov.pagopa.cgn.portal.controller.backoffice;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.*;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.FailureReason;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.SuspendDiscount;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeAgreementApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AzureStorage azureStorage;

    private MockMultipartFile multipartFile;

    @BeforeEach
    void beforeEach() throws IOException {
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder().connectionString(
                getAzureConnectionString()).containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }

        setAdminAuth();
    }

    @Test
    void GetAgreements_GetAgreementsPending_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity pendingAgreement = agreementTestObject.getAgreementEntity();
        List<DiscountEntity> discounts = agreementTestObject.getDiscountEntityList();
        DiscountEntity discountEntity = discounts.get(0);
        this.mockMvc.perform(get(TestUtils.AGREEMENT_REQUESTS_CONTROLLER_PATH))
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
                    .andExpect(jsonPath("$.items[0].documents", hasSize(1)))
        			.andExpect(jsonPath("$.items[0].entityType").value(EntityType.PUBLICADMINISTRATION.getValue()));

    }

    @Test
    void GetAgreements_GetAssignedToMeAgreements_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithStatusFilterPath("AssignedAgreement",
                                                                                    Optional.of("Me"))))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PUBLICADMINISTRATION.getValue()));
    }

    @Test
    void GetAgreements_GetAssignedToOtherAgreements_NotFound_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithStatusFilterPath("AssignedAgreement",
                                                                                    Optional.of("Others"))))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void GetAgreements_GetPendingAgreement_NotFound_Ok() throws Exception {
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithStatusFilterPath("PendingAgreement",
                                                                                    Optional.empty())))
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
                                                                            .sorted(Comparator.comparing(a -> a.getProfileEntity()
                                                                                                               .getFullName()))
                                                                            .map(AgreementTestObject::getAgreementEntity)
                                                                            .collect(Collectors.toList());
        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.OPERATOR,
                                                                                Sort.Direction.ASC)))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items", hasSize(5)))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PRIVATE.getValue()))
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
                                                                               .sorted(Comparator.comparing(
                                                                                                         AgreementEntity::getRequestApprovalTime)
                                                                                                 .reversed())
                                                                               .collect(Collectors.toList());

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.REQUEST_DATE,
                                                                                Sort.Direction.DESC)))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items", hasSize(5)))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PUBLICADMINISTRATION.getValue()))                    
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
                                                                  .map(AgreementTestObject::getAgreementEntity)
                                                                  .collect(Collectors.toList());
        Assertions.assertEquals(5, agreementEntityList.size());
        backofficeAgreementService.assignAgreement(agreementEntityList.get(2).getId());

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.ASSIGNEE,
                                                                                Sort.Direction.ASC)))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items", hasSize(5)))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PRIVATE.getValue()))
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
                                                                  .map(AgreementTestObject::getAgreementEntity)
                                                                  .collect(Collectors.toList());
        Assertions.assertEquals(5, agreementEntityList.size());
        AgreementEntity assignedAgreement = agreementEntityList.get(2);
        assignedAgreement = backofficeAgreementService.assignAgreement(assignedAgreement.getId());


        this.mockMvc.perform(get(TestUtils.getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum.STATE,
                                                                                Sort.Direction.ASC)))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items", hasSize(5)))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PRIVATE.getValue()))
                    .andExpect(jsonPath("$.items[0].assignee").doesNotExist())
                    .andExpect(jsonPath("$.items[1].assignee").doesNotExist())
                    .andExpect(jsonPath("$.items[2].assignee").doesNotExist())
                    .andExpect(jsonPath("$.items[3].assignee").doesNotExist())
                    .andExpect(jsonPath("$.items[4].id").value(assignedAgreement.getId()))
                    .andExpect(jsonPath("$.items[4].assignee.fullName").value(assignedAgreement.getBackofficeAssignee()));

    }

    @Test
    void DeleteDocument_DeleteDocument_NoContent() throws Exception {
        String documentTypeDto = "AdhesionRequest";
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        DocumentEntity document = TestUtils.createDocument(pendingAgreement,
                                                           DocumentTypeEnum.BACKOFFICE_ADHESION_REQUEST);
        documentRepository.save(document);
        this.mockMvc.perform(delete(TestUtils.getBackofficeDocumentPath(pendingAgreement.getId()) +
                                    "/" +
                                    documentTypeDto)).andDo(log()).andExpect(status().isNoContent());

    }

    @Test
    void DeleteDocument_DeleteDocumentNotFound_BadRequest() throws Exception {
        String documentTypeDto = "AdhesionRequest";
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        this.mockMvc.perform(delete(TestUtils.getBackofficeDocumentPath(pendingAgreement.getId()) +
                                    "/" +
                                    documentTypeDto)).andDo(log()).andExpect(status().isBadRequest());

    }

    @Test
    void DeleteDocument_DeleteDocumentWithWrongType_BadRequest() throws Exception {
        String documentTypeDto = "Invalid";
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        this.mockMvc.perform(delete(TestUtils.getBackofficeDocumentPath(agreementEntity.getId()) +
                                    "/" +
                                    documentTypeDto)).andDo(log()).andExpect(status().isBadRequest());

    }

    @Test
    void GetDocuments_GetDocuments_Ok() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        List<DocumentEntity> documents = TestUtils.createSampleBackofficeDocumentList(agreementEntity);
        documentRepository.saveAll(documents);

        this.mockMvc.perform(get(TestUtils.getBackofficeDocumentPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.*", hasSize(1)))
                    .andExpect(jsonPath("$.[0].documentUrl").isNotEmpty())
                    .andExpect(jsonPath("$.[0].creationDate").value(LocalDate.now().toString()));
    }

    @Test
    void GetDocuments_GetDocumentNotFound_Ok() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        this.mockMvc.perform(get(TestUtils.getBackofficeDocumentPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.*", hasSize(0)));
    }

    @Test
    void Action_SuspendDiscountNotPublished_BadRequest () throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.LANDINGPAGE);
        profileService.createProfile(profileEntity, agreementEntity.getId());


        DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreementEntity);
        discount.setLandingPageUrl("fake url");
        discount = discountService.createDiscount(agreementEntity.getId(), discount).getDiscountEntity();

        // simulate test passed
        discount.setState(DiscountStateEnum.TEST_PASSED);
        discount = discountRepository.save(discount);

        SuspendDiscount suspendDiscount = TestUtils.suspendableDiscountFromDiscountEntity(discount);

        // suspend
        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountSuspendingPath(agreementEntity.getId(),discount.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(suspendDiscount)))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.CANNOT_SUSPEND_DISCOUNT_NOT_PUBLISHED.getValue()));
    }

    @Test
    void Action_SuspendDiscountWithoutProfile_BadRequest () throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.LANDINGPAGE);
        profileService.createProfile(profileEntity, agreementEntity.getId());


        DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreementEntity);
        discount.setLandingPageUrl("fake url");
        discount = discountService.createDiscount(agreementEntity.getId(), discount).getDiscountEntity();

        // simulate test passed
        discount.setState(DiscountStateEnum.TEST_PASSED);
        discount = discountRepository.save(discount);

        //force delete of profile
        profileRepository.delete(profileEntity);

        SuspendDiscount suspendDiscount = TestUtils.suspendableDiscountFromDiscountEntity(discount);

        // suspend
        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountSuspendingPath(agreementEntity.getId(),discount.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(suspendDiscount)))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
    }

    @Test
    void GetBucketCode_Found_Ok() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);

        // set discount to TEST_PENDING
        discountEntity.setState(DiscountStateEnum.TEST_PENDING);
        discountRepository.save(discountEntity);

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsDiscountBucketCodePath(agreementEntity.getId(),
                                                                                      discountEntity.getId()
                                                                                                    .toString())))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.*", hasSize(1)))
                    .andExpect(jsonPath("$.code").isNotEmpty());

        List<DiscountBucketCodeEntity> codes = discountBucketCodeRepository.findAllByDiscount(discountEntity);
        Assertions.assertEquals(2, codes.size());

        List<DiscountBucketCodeEntity> usedCodes = codes.stream()
                                                        .filter(DiscountBucketCodeEntity::getIsUsed)
                                                        .collect(Collectors.toList());
        Assertions.assertEquals(1, usedCodes.size());

        List<DiscountBucketCodeEntity> unusedCodes = codes.stream()
                                                          .filter(c -> !c.getIsUsed())
                                                          .collect(Collectors.toList());
        Assertions.assertEquals(1, unusedCodes.size());
    }

    @Test
    void GetBucketCode_NotFound_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        String notExistingDiscountId = "-1";

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsDiscountBucketCodePath(agreementEntity.getId(),
                        notExistingDiscountId)))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.DISCOUNT_NOT_FOUND.getValue()));
    }

    @Test
    void GetBucketCode_DiscountStateNotInTestPending_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsDiscountBucketCodePath(agreementEntity.getId(),
                        discountEntity.getId().toString())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.CANNOT_GET_BUCKET_CODE_FOR_DISCOUNT_NOT_IN_TEST_PENDING.getValue()));
    }

    @Test
    void GetBucketCode_WithoutProfile_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);
        discountEntity.setState(DiscountStateEnum.TEST_PENDING);
        discountRepository.save(discountEntity);
        //force delete profile
        profileRepository.delete(profileRepository.findByAgreementId(agreementEntity.getId()).get());

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsDiscountBucketCodePath(agreementEntity.getId(),
                        discountEntity.getId().toString())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
    }

    @Test
    void GetBucketCode_WithProfileNoBucket_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);
        discountEntity.setState(DiscountStateEnum.TEST_PENDING);
        discountRepository.save(discountEntity);

        ProfileEntity pe = profileRepository.findByAgreementId(agreementEntity.getId()).get();
        pe.setDiscountCodeType(DiscountCodeTypeEnum.STATIC);
        profileRepository.save(pe);

        this.mockMvc.perform(get(TestUtils.getAgreementRequestsDiscountBucketCodePath(agreementEntity.getId(),
                        discountEntity.getId().toString())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.CANNOT_GET_BUCKET_CODE_FOR_DISCOUNT_NO_BUCKET.getValue()));
    }

    @Test
    void SetDiscountTestPassed_NoContent() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);

        // set discount to TEST_PENDING
        discountEntity.setState(DiscountStateEnum.TEST_PENDING);
        discountRepository.save(discountEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestPassedPath(agreementEntity.getId(),
                                                                                       discountEntity.getId()
                                                                                                     .toString())))
                    .andDo(log())
                    .andExpect(status().isNoContent());

        // assert discount is in TEST_PASSED
        discountEntity = discountRepository.findById(discountEntity.getId()).orElseThrow();
        Assertions.assertEquals(DiscountStateEnum.TEST_PASSED, discountEntity.getState());
        Assertions.assertNull(discountEntity.getTestFailureReason());
    }

    @Test
    void SetDiscountTestPassed_NotTestPending_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestPassedPath(agreementEntity.getId(),
                                                                                       discountEntity.getId()
                                                                                                     .toString())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_SET_DISCOUNT_STATE_FOR_DISCOUNT_NOT_IN_TEST_PENDING.getValue()));
    }

    @Test
    void SetDiscountTestFailed_NoContent() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);

        // set discount to TEST_PENDING
        discountEntity.setState(DiscountStateEnum.TEST_PENDING);
        discountRepository.save(discountEntity);

        FailureReason failureReason = new FailureReason();
        failureReason.setReasonMessage("A reason");

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestFailedPath(agreementEntity.getId(),
                                                                                       discountEntity.getId()
                                                                                                     .toString())).contentType(
                    MediaType.APPLICATION_JSON).content(TestUtils.getJson(failureReason)))
                    .andDo(log())
                    .andExpect(status().isNoContent());

        // assert discount is in TEST_FAILED with a reason
        discountEntity = discountRepository.findById(discountEntity.getId()).orElseThrow();
        Assertions.assertEquals(DiscountStateEnum.TEST_FAILED, discountEntity.getState());
        Assertions.assertEquals(failureReason.getReasonMessage(), discountEntity.getTestFailureReason());
    }

    @Test
    void SetDiscountTestFailed_NotTestPending_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        DiscountEntity discountEntity = createDiscountAndApproveAgreement(agreementEntity);

        FailureReason failureReason = new FailureReason();
        failureReason.setReasonMessage("A reason");

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestFailedPath(agreementEntity.getId(),
                                                                                       discountEntity.getId()
                                                                                                     .toString())).contentType(
                    MediaType.APPLICATION_JSON).content(TestUtils.getJson(failureReason)))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_SET_DISCOUNT_STATE_FOR_DISCOUNT_NOT_IN_TEST_PENDING.getValue()));
    }

    private DiscountEntity createDiscountAndApproveAgreement(AgreementEntity agreementEntity) throws IOException {
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.BUCKET);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
                               discountEntity.getLastBucketCodeLoadUid(),
                               multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // load a couple bucket codes and await
        bucketService.performBucketLoad(discountEntity.getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);

        // approve agreement
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setStartDate(LocalDate.now());
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementRepository.save(agreementEntity);

        return discountEntity;
    }

    @Test
    void approval_WithoutMandatoryDocuments_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        agreementEntity = agreementServiceLight.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setBackofficeAssignee(TestUtils.FAKE_ID);
        agreementRepository.save(agreementEntity);

        List<DocumentEntity> entities = documentRepository.findByAgreementId(agreementEntity.getId());
        documentRepository.delete(entities.get(0));

        this.mockMvc.perform(post(TestUtils.getAgreementRequestApprovalPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.MANDATORY_DOCUMENT_ARE_MISSING.getValue()));
    }

    @Test
    void approval_WithAgreementNotAssignedToCurrentUser_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        agreementEntity = agreementServiceLight.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementRepository.save(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestApprovalPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.AGREEMENT_NOT_ASSIGNED_TO_CURRENT_USER.getValue()));
    }

    @Test
    void approval_WithoutAgreementInPendingState_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestApprovalPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.CANNOT_PROCEED_AGREEMENT_NOT_IN_PENDING.getValue()));
    }

    @Test
    void approval_alreadyAssignedAgreement_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        agreementEntity = agreementServiceLight.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setBackofficeAssignee(TestUtils.FAKE_ID);
        agreementRepository.save(agreementEntity);

        List<DocumentEntity> entities = documentRepository.findByAgreementId(agreementEntity.getId());
        documentRepository.delete(entities.get(0));

        this.mockMvc.perform(put(TestUtils.getAgreementRequestAssigneePath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.AGREEMENT_ALREADY_ASSIGNED_TO_CURRENT_USER.getValue()));
    }

    @Test
    void approval_alreadyUnassignedAgreement_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        agreementEntity = agreementServiceLight.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setBackofficeAssignee(TestUtils.FAKE_ID_2);
        agreementRepository.save(agreementEntity);

        List<DocumentEntity> entities = documentRepository.findByAgreementId(agreementEntity.getId());
        documentRepository.delete(entities.get(0));

        this.mockMvc.perform(delete(TestUtils.getAgreementRequestAssigneePath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.AGREEMENT_NOT_ASSIGNED_TO_CURRENT_USER.getValue()));
    }

    @Test
    void approval_noLongerAssignedAgreement_BadRequest() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        createDiscountAndApproveAgreement(agreementEntity);

        agreementEntity = agreementServiceLight.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setBackofficeAssignee(null);
        agreementRepository.save(agreementEntity);

        List<DocumentEntity> entities = documentRepository.findByAgreementId(agreementEntity.getId());
        documentRepository.delete(entities.get(0));

        this.mockMvc.perform(delete(TestUtils.getAgreementRequestAssigneePath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(content().string(ErrorCodeEnum.AGREEMENT_NO_LONGER_ASSIGNED.getValue()));
    }
}
