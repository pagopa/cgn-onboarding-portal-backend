package it.gov.pagopa.cgn.portal.controller;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.*;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementTerminationAction;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.model.CompletedStep;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import it.gov.pagopa.cgnonboardingportal.model.HelpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AgreementApiTest
        extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ConfigProperties configProperties;

    @BeforeEach
    void beforeEach() {
        setOperatorAuth();
        ReflectionTestUtils.setField(configProperties, "bucketMinCsvRows", 0);
    }

    @Test
    void Create_CreateAgreement_Ok()
            throws Exception {
        this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                         EntityType.PRIVATE,
                                                         TestUtils.FAKE_ORGANIZATION_NAME);

        this.mockMvc.perform(post(TestUtils.AGREEMENTS_CONTROLLER_PATH))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.DRAFT_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.imageUrl").isEmpty())
                    .andExpect(jsonPath("$.completedSteps").isEmpty())
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void GetAgreement_GetAgreementWithProfile_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.DRAFT_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.imageUrl").isEmpty())
                    .andExpect(jsonPath("$.completedSteps").isArray())
                    .andExpect(jsonPath("$.completedSteps", hasSize(1)))
                    .andExpect(jsonPath("$.completedSteps[0]").value(CompletedStep.PROFILE.getValue()))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void GetAgreement_GetAgreementWithoutAgreementUser_BadRequest()
            throws Exception {
        this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                         EntityType.PRIVATE,
                                                         TestUtils.FAKE_ORGANIZATION_NAME);
        AgreementUserEntity entities = agreementUserRepository.findById(TestUtils.FAKE_ID)
                                                              .orElse(new AgreementUserEntity());
        agreementRepository.deleteById(entities.getAgreementId());
        agreementUserRepository.deleteById(TestUtils.FAKE_ID);
        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.AGREEMENT_USER_NOT_FOUND.getValue()));
    }

    @Test
    void GetAgreement_GetAgreementWithProfileAndDiscount_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.DRAFT_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.imageUrl").isEmpty())
                    .andExpect(jsonPath("$.completedSteps").isArray())
                    .andExpect(jsonPath("$.completedSteps", hasSize(2)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));

    }

    @Test
    void GetAgreement_GetAgreementWithAllStepsCompleted_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        List<DocumentEntity> documentList = TestUtils.createSampleDocumentList(agreementEntity);
        documentRepository.saveAll(documentList);
        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.DRAFT_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.imageUrl").isEmpty())
                    .andExpect(jsonPath("$.completedSteps").isArray())
                    .andExpect(jsonPath("$.completedSteps", hasSize(3)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));

    }

    @Test
    void GetAgreement_GetApprovedAgreement_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveApprovedAgreement(agreementEntity);
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());

        Assertions.assertEquals(AgreementStateEnum.APPROVED, agreementEntity.getState());

        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.APPROVED_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.completedSteps", hasSize(CompletedStep.values().length)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void GetAgreement_GetActiveAgreement_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        saveApprovedAgreement(agreementEntity);

        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);
        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());

        Assertions.assertEquals(AgreementStateEnum.ACTIVE, agreementEntity.getState());

        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.ACTIVE_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.completedSteps", hasSize(CompletedStep.values().length)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void GetAgreement_GetInactiveAgreement_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        saveApprovedAgreement(agreementEntity);

        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.INACTIVE);
        agreementRepository.save(agreementEntity);

        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.INACTIVE_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.completedSteps", hasSize(CompletedStep.values().length)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void GetAgreement_GetTerminationInProgressAgreement_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        saveApprovedAgreement(agreementEntity);

        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.INACTIVE);
        agreementRepository.save(agreementEntity);

        backofficeAgreementService.manageAgreementTermination(agreementEntity.getId(),
                                      AgreementTerminationAction.START_TERMINATION_IN_PROGRESS);

        setOperatorAuth();
        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.TERMINATION_IN_PROGRESS_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.completedSteps", hasSize(CompletedStep.values().length)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void GetAgreement_GetTerminatedAgreement_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        saveApprovedAgreement(agreementEntity);

        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.INACTIVE);
        agreementRepository.save(agreementEntity);

        backofficeAgreementService.manageAgreementTermination(agreementEntity.getId(),
                                                              AgreementTerminationAction.START_TERMINATION_IN_PROGRESS);
        backofficeAgreementService.manageAgreementTermination(agreementEntity.getId(),
                                                              AgreementTerminationAction.COMPLETE_TERMINATION);

        setOperatorAuth();
        this.mockMvc.perform(post(TestUtils.createAgreements()))
                    .andDo(log())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value(AgreementState.TERMINATED_AGREEMENT.getValue()))
                    .andExpect(jsonPath("$.completedSteps", hasSize(CompletedStep.values().length)))
                    .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    @Test
    void SendHelpRequest_SendHelpRequestWithTerminatedAgreement_Forbidden()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        saveApprovedAgreement(agreementEntity);

        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.INACTIVE);
        agreementRepository.save(agreementEntity);

        backofficeAgreementService.manageAgreementTermination(agreementEntity.getId(),
                                                              AgreementTerminationAction.START_TERMINATION_IN_PROGRESS);
        backofficeAgreementService.manageAgreementTermination(agreementEntity.getId(),
                                                              AgreementTerminationAction.COMPLETE_TERMINATION);

        HelpRequest helpRequest = TestUtils.createSampleAuthenticatedHelpRequest();

        setOperatorAuth();
        this.mockMvc.perform(post(TestUtils.getAuthenticatedHelpPath(agreementEntity.getId()))
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(TestUtils.getJson(helpRequest)))
                    .andDo(log())
                    .andExpect(status().isForbidden());
    }

    @Test
    void RequestApproval_RequestApproval_NoContent()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        List<DocumentEntity> documentList = TestUtils.createSampleDocumentList(agreementEntity);
        documentRepository.saveAll(documentList);

        this.mockMvc.perform(post(TestUtils.getAgreementApprovalPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isNoContent());
    }

        @Test
        void RequestApproval_RequestApproval_ShouldWriteChangeAuditRow()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                   EntityType.PRIVATE,
                                                   TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));

        this.mockMvc.perform(post(TestUtils.getAgreementApprovalPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(status().isNoContent());

        List<ChangeAuditEntity> audits = changeAuditRepository.findAll();

        Assertions.assertEquals(2, audits.size());
        Assertions.assertTrue(audits.stream().anyMatch(audit ->
            ChangeAuditSubjectTypeEnum.AGREEMENT.equals(audit.getSubjectType()) &&
            ChangeAuditOperationTypeEnum.INSERT.equals(audit.getOperationType()) &&
            agreementEntity.getId().equals(audit.getSubjectId())));

        ChangeAuditEntity updateAudit = audits.stream()
                              .filter(audit -> ChangeAuditSubjectTypeEnum.AGREEMENT.equals(
                                  audit.getSubjectType()))
                              .filter(audit -> ChangeAuditOperationTypeEnum.UPDATE.equals(
                                  audit.getOperationType()))
                              .findFirst()
                              .orElseThrow();

        Assertions.assertEquals(agreementEntity.getId(), updateAudit.getSubjectId());
        Assertions.assertEquals(TestUtils.FAKE_ID, updateAudit.getActorRef());
        Assertions.assertEquals(profileEntity.getFullName(), updateAudit.getPartnerFullName());
        Assertions.assertNotNull(updateAudit.getInsertTime());
        Assertions.assertEquals("PENDING", updateAudit.getValue().get("state"));
        Assertions.assertEquals(agreementEntity.getId(), updateAudit.getValue().get("agreement_k"));
        Assertions.assertNotNull(updateAudit.getValue().get("request_approval_time"));
        Assertions.assertEquals(TestUtils.FAKE_ORGANIZATION_NAME,
                    updateAudit.getValue().get("organization_name"));
        }

    @Test
    void RequestApproval_RequestApprovalWithoutDiscount_BadRequest()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        this.mockMvc.perform(post(TestUtils.getAgreementApprovalPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutDocuments_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementApprovalPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.AGREEMENT_NOT_APPROVABLE_FOR_WRONG_MANDATORY_DOCUMENTS.getValue()));
    }

    @Test
    void RequestApproval_RequestApprovalProfileNotFound_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);

        this.mockMvc.perform(post(TestUtils.getAgreementApprovalPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
    }

    @Test
    void RequestApproval_RequestApprovalDiscountNotFound_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);

        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        this.mockMvc.perform(post(TestUtils.getAgreementApprovalPath(agreementEntity.getId())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.DISCOUNT_NOT_FOUND.getValue()));
    }

    @Test
    void PublishDiscount_PublishDiscount_NoContent()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        saveApprovedAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isNoContent());
    }

    @Test
    void TestDiscount_TestDiscountWithSalesChannelOffline_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        saveApprovedAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestingPath(agreementEntity.getId(),
                                                                                    discountEntity.getId().toString())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_TEST_DISCOUNTS_WITH_OFFLINE_MERCHANTS.getValue()));
    }

    @Test
    void TestDiscount_TestDiscountWithoutProfile_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);

        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        //force delete profile after discount creation
        profileRepository.delete(profileEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestingPath(agreementEntity.getId(),
                                                                                    discountEntity.getId().toString())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
    }

    @Test
    void TestDiscount_TestDiscountUpdateInformationLastUpdateDate_Ok()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);

        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        saveApprovedAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getAgreementRequestsDiscountTestingPath(agreementEntity.getId(),
                                                                                    discountEntity.getId().toString())))
                    .andDo(log())
                    .andExpect(status().isNoContent());
        Optional<AgreementEntity> entity = agreementRepository.findById(profileEntity.getAgreement().getId());
        Assertions.assertTrue(entity.isPresent());
        Assertions.assertNotNull(entity.get().getInformationLastUpdateDate());
    }

    @Test
    void PublishDiscount_PublishWithProfileDiscountCodeTypeDifferentFromStatic_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.STATIC);
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        discountEntity.setStaticCode(null);
        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        saveApprovedAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_HAVE_EMPTY_STATIC_CODE_FOR_PROFILE_WITH_STATIC_CODE.getValue()));
    }

    @Test
    void PublishDiscount_PublishWithProfileDiscountCodeTypeLendingWithoutUrl_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.LANDINGPAGE);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        //I temporarily set the setLandingPageUrl for bypass check and immediately after call save it to null to trigger the same check during the mockMvc
        discountEntity.setLandingPageUrl("link_fake");
        discountEntity.setLandingPageReferrer("referrer");
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        discountEntity.setLandingPageUrl(null);
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        saveApprovedAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_HAVE_EMPTY_LANDING_PAGE_URL_FOR_PROFILE_LANDING_PAGE.getValue()));
    }

    @Test
    void PublishDiscount_PublishWithProfileDiscountCodeTypeBucketWithBucketLoadInProgress_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.BUCKET);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        discountEntity.setLastBucketCodeLoadUid("fake_uid");
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        saveApprovedAgreement(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_PROCEED_WITH_DISCOUNT_WITH_BUCKET_LOAD_IN_PROGRESS.getValue()));
    }

    @Test
    void PublishDiscount_PublishDiscountOfNotApprovedAgreement_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_PROCEED_WITH_DISCOUNT_WITH_NOT_APPROVED_AGREEMENT.getValue()));
    }

    @Test
    void PublishDiscount_PublishWithSuspendedDiscount_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setState(DiscountStateEnum.SUSPENDED);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_PROCEED_WITH_SUSPENDED_DISCOUNT.getValue()));
    }

    @Test
    void PublishDiscount_PublishDiscountWithExpiredDate_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().minusDays(2));
        discountEntity.setEndDate(LocalDate.now().minusDays(1));
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_PROCEED_WITH_EXPIRED_DISCOUNT.getValue()));
    }

    @Test
    void PublishDiscount_PublishDiscountOnlineWithNoTestPassed_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        discountEntity.setEndDate(LocalDate.now().minusDays(2));
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.CANNOT_PROCEED_WITH_ONLINE_DISCOUNT_WITH_NOT_PASSED_TEST.getValue()));
    }

    @Test
    void PublishDiscount_PublishDiscountWithTwoCategories_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        //simulate more categories
        discountEntity.getProducts().addAll(TestUtils.getProductEntityList(discountEntity));
        discountEntity.getProducts().addAll(TestUtils.getProductEntityList(discountEntity));
        discountEntity.getProducts().get(1).setProductCategory(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT);
        discountEntity.getProducts().get(2).setProductCategory(ProductCategoryEnum.LEARNING);
        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.DISCOUNT_CANNOT_HAVE_MORE_THAN_TWO_CATEGORIES.getValue()));
    }

    @Test
    void PublishDiscount_PublishDiscountWithoutProfile_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        profileRepository.delete(profileEntity);

        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
    }

    @Test
    void PublishDiscount_ReachedMaxPublishableDiscounts_BadRequest()
            throws Exception {

        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);

        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        // keep the fixture semantically aligned with current published discounts
        agreementEntity.setState(AgreementStateEnum.ACTIVE);
        agreementRepository.save(agreementEntity);

        for (int i = 0; i < 5; i++) {
            // creating discount
            DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
            discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                                            .getDiscountEntity();

            // simulate already published current discounts through the real publish flow
            discountEntity.setState(DiscountStateEnum.TEST_PASSED);
            discountEntity = discountRepository.save(discountEntity);
            discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        }

        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.ACTIVE, agreementEntity.getState());

        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);


        this.mockMvc.perform(post(TestUtils.getDiscountPublishingPath(agreementEntity.getId(), discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.MAX_NUMBER_OF_PUBLISHABLE_DISCOUNTS_REACHED.getValue()));
    }

    @Test
    void SuspendDiscount_UnpublishDiscount_NoContent()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        saveApprovedAgreement(agreementEntity);

        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());

        this.mockMvc.perform(post(TestUtils.getDiscountUnpublishingPath(agreementEntity.getId(),
                                                                        discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isNoContent());
    }

    @Test
    void SuspendDiscount_UnpublishDiscount_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        saveApprovedAgreement(agreementEntity);

        // if we don't publish discount we expect an InvalidRequestException
        this.mockMvc.perform(post(TestUtils.getDiscountUnpublishingPath(agreementEntity.getId(),
                                                                        discountEntity.getId())))
                    .andDo(log())
                    .andExpect(status().isBadRequest());
    }

    @Test
    void UploadBucket_UploadValidBucketWithoutProfile_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));

        MockMultipartFile multipartFile = new MockMultipartFile("document",
                                                                "test-codes.csv",
                                                                "multipart/form-data",
                                                                csv);
        createBlobDocument();
        this.mockMvc.perform(multipart(TestUtils.getUploadBucketPath(agreementEntity.getId())).file(multipartFile))
                    .andDo(log())
                    .andExpect(status().isBadRequest());
    }

    @Test
    void UploadBucket_UploadInvalidBucket_BadRequest()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        byte[] csv = "sample".getBytes();

        MockMultipartFile multipartFile = new MockMultipartFile("document",
                                                                "test-codes.pdf",
                                                                "multipart/form-data",
                                                                csv);
        createBlobDocument();
        this.mockMvc.perform(multipart(TestUtils.getUploadBucketPath(agreementEntity.getId())).file(multipartFile))
                    .andDo(log())
                    .andExpect(status().isBadRequest());
    }

    @Test
    void UploadBucket_UploadValidBucket_Ok()
            throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity,
                                                                          SalesChannelEnum.ONLINE,
                                                                          DiscountCodeTypeEnum.BUCKET);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));

        MockMultipartFile multipartFile = new MockMultipartFile("document",
                                                                "test-codes.csv",
                                                                "multipart/form-data",
                                                                csv);
        createBlobDocument();
        this.mockMvc.perform(multipart(TestUtils.getUploadBucketPath(agreementEntity.getId())).file(multipartFile))
                    .andDo(log())
                    .andExpect(status().isOk());
    }

    @Test
    void UploadImage_UploadValidPngImage_Ok()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MockMultipartFile multipartFile = new MockMultipartFile("image", "test-image.png", "image/png", image);
        createBlobImage();
        this.mockMvc.perform(multipart(TestUtils.getUploadImagePath(agreementEntity.getId())).file(multipartFile))
                    .andDo(log())
                    .andExpect(status().isOk());
    }

    @Test
    void UploadImage_UploadInValidImage_GetInvalidImageErrorCode()
            throws Exception {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));

        MockMultipartFile multipartFile = new MockMultipartFile("image", "test-image.pdf", "image/png", image);
        createBlobImage();
        this.mockMvc.perform(multipart(TestUtils.getUploadImagePath(agreementEntity.getId())).file(multipartFile))
                    .andDo(log())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue()));
    }

    private void createBlobImage() {
        BlobContainerClient imageContainer = new BlobContainerClientBuilder().connectionString(getAzureConnectionString())
                                                                             .containerName(configProperties.getImagesContainerName())
                                                                             .buildClient();
        if (!imageContainer.exists()) {
            imageContainer.create();
        }
    }

    private void createBlobDocument() {
        BlobContainerClient documentContainer = new BlobContainerClientBuilder().connectionString(
                getAzureConnectionString()).containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainer.exists()) {
            documentContainer.create();
        }
    }

}
