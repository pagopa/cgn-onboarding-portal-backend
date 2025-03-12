package it.gov.pagopa.cgn.portal.service.backoffice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.LogMemoryAppender;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.BackofficeAgreementService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles({"dev"})
class BackofficeAgreementServiceTest
        extends IntegrationAbstractTest {

    @Autowired
    private BackofficeAgreementService backofficeAgreementService;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
    }

    @Test
    void GetAgreement_GetAgreementWithoutFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder().build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        AgreementEntity retrievedAgreement = page.getContent().get(0);
        Assertions.assertEquals(pendingAgreement.getId(), retrievedAgreement.getId());
        Assertions.assertNotNull(retrievedAgreement.getEntityType());
    }

    @Test
    void GetAgreement_GetAgreementWithProfileFullNameFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .profileFullName(pendingAgreement.getProfile().getFullName())
                                                  .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement.getId(), page.getContent().get(0).getId());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }

    @Test
    void GetAgreement_GetAgreementWithProfileFullNameLowerCaseFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .profileFullName(pendingAgreement.getProfile()
                                                                                   .getFullName()
                                                                                   .toLowerCase())
                                                  .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement.getId(), page.getContent().get(0).getId());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateFromFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder().dateFrom(LocalDate.now().minusDays(2)).build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement.getId(), page.getContent().get(0).getId());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateToFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder().dateTo(LocalDate.now().plusMonths(1)).build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement.getId(), page.getContent().get(0).getId());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateFromAndToFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .dateFrom(LocalDate.now().minusDays(2))
                                                  .dateTo(LocalDate.now().plusMonths(1))
                                                  .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        AgreementEntity retrievedAgreement = page.getContent().get(0);
        Assertions.assertEquals(pendingAgreement.getId(), retrievedAgreement.getId());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateFuture_AgreementNotFound() {
        createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .dateFrom(LocalDate.now().plusDays(2))
                                                  .dateTo(LocalDate.now().plusMonths(1))
                                                  .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(0L, page.getTotalElements());
        Assertions.assertEquals(0, page.getTotalPages());
        Assertions.assertTrue(CollectionUtils.isEmpty(page.getContent()));
    }

    @Test
    void GetAgreement_GetAgreementWithAssignedStatus_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        pendingAgreement = backofficeAgreementService.assignAgreement(pendingAgreement.getId());
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .agreementState(AgreementState.PENDING_AGREEMENT.getValue())
                                                  .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        AgreementEntity retrievedAgreement = page.getContent().get(0);
        Assertions.assertEquals(pendingAgreement.getId(), retrievedAgreement.getId());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }


    @Test
    void AssignAgreement_AssignAgreement_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        AgreementEntity agreementEntity = backofficeAgreementService.assignAgreement(pendingAgreement.getId());
        Assertions.assertTrue(StringUtils.isNotBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
        Assertions.assertNotNull(pendingAgreement.getEntityType());
    }

    @Test
    void AssignAgreement_AssignAgreementMultipleTimes_ThrowException() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        var agreementId = pendingAgreement.getId();

        AgreementEntity agreementEntity = backofficeAgreementService.assignAgreement(agreementId);

        Assertions.assertTrue(StringUtils.isNotBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> backofficeAgreementService.assignAgreement(agreementId));

    }

    @Test
    void AssignAgreement_AssignAgreementWithStatusDraft_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        var agreementId = agreementEntity.getId();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity = profileService.createProfile(profileEntity, agreementId);
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementId, discountEntity);
        saveSampleDocuments(agreementEntity);
        //agreement state not PENDING

        Assertions.assertThrows(InvalidRequestException.class,
                                () -> backofficeAgreementService.assignAgreement(agreementId));
    }

    @Test
    void UnassignAgreement_UnassignAgreement_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        backofficeAgreementService.assignAgreement(pendingAgreement.getId());

        AgreementEntity agreementEntity = backofficeAgreementService.unassignAgreement(pendingAgreement.getId());
        Assertions.assertTrue(StringUtils.isBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
    }

    @Test
    void UnassignAgreement_UnassignAgreementWithoutAssignment_ThrowException() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        var agreementId = pendingAgreement.getId();

        Assertions.assertThrows(InvalidRequestException.class,
                                () -> backofficeAgreementService.unassignAgreement(agreementId));
        AgreementEntity agreementEntity = agreementService.findAgreementById(agreementId);
        Assertions.assertTrue(StringUtils.isBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
    }

    @Test
    void ApproveAgreement_ApproveAgreement_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(pendingAgreement));
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(pendingAgreement.getId());

        Assertions.assertNotNull(approveAgreement.getEntityType());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());
    }

    @Test
    void ApproveAgreement_ApproveAgreementSaleChannelBoth_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement(SalesChannelEnum.BOTH,
                                                                  DiscountCodeTypeEnum.API,
                                                                  true).getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(pendingAgreement));
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(pendingAgreement.getId());
        Assertions.assertNotNull(approveAgreement.getEntityType());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());
    }

    @Test
    void ApproveAgreement_ApproveAgreementWithoutBackofficeDocuments_InvalidRequestException() {
        AgreementEntity pendingAgreement = createPendingAgreement(SalesChannelEnum.BOTH,
                                                                  DiscountCodeTypeEnum.API,
                                                                  true).getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        final String agreementId = pendingAgreement.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> backofficeAgreementService.approveAgreement(agreementId));
    }

    @Test
    void ApproveAgreement_ApproveAgreementSaleChannelOffline_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement(SalesChannelEnum.OFFLINE,
                                                                  DiscountCodeTypeEnum.STATIC,
                                                                  true).getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(pendingAgreement));
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(pendingAgreement.getId());
        Assertions.assertNotNull(approveAgreement.getEntityType());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());
    }

    @Test
    void ApproveAgreement_ApproveAgreementSaleChannelOnlineWithStaticCode_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement(SalesChannelEnum.ONLINE,
                                                                  DiscountCodeTypeEnum.STATIC,
                                                                  true).getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(pendingAgreement));
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(pendingAgreement.getId());
        Assertions.assertNotNull(approveAgreement.getEntityType());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());
    }

    @Test
    void ApproveAgreement_ApproveAgreementSaleChannelOnlineWithApiCode_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement(SalesChannelEnum.ONLINE,
                                                                  DiscountCodeTypeEnum.API,
                                                                  true).getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(pendingAgreement));
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(pendingAgreement.getId());
        Assertions.assertNotNull(approveAgreement.getEntityType());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());
    }

    @Test
    void ApproveAgreement_ApproveAgreementSaleChannelOnlineWithoutDiscountCodeType_LogError() {
        Logger logger = (Logger) LoggerFactory.getLogger("it.gov.pagopa.cgn.portal.email.EmailNotificationFacade");
        var memoryAppender = new LogMemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.ERROR);
        logger.addAppender(memoryAppender);
        memoryAppender.start();

        AgreementEntity pendingAgreement = createPendingAgreement(SalesChannelEnum.ONLINE,
                                                                  null,
                                                                  true).getAgreementEntity();
        pendingAgreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        pendingAgreement = agreementRepository.save(pendingAgreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(pendingAgreement));
        var agreementId = pendingAgreement.getId();
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(agreementId);
        Assertions.assertNotNull(approveAgreement.getEntityType());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());

        Assertions.assertTrue(memoryAppender.contains(
                "Failed to send Agreement Request Approved notification to: referent.registry@pagopa.it",
                Level.ERROR));
        Assertions.assertTrue(memoryAppender.contains("An online merchant must have a Discount Code validation type set",
                                                      Level.ERROR));
    }

    @Test
    void ApproveAgreement_ApproveAgreementWithDraftStatus_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        //agreement state not PENDING
        final String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> backofficeAgreementService.approveAgreement(agreementId));
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());

        Assertions.assertNotEquals(AgreementStateEnum.APPROVED, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());

    }

    @Test
    void RejectAgreement_RejectAgreement_Ok() {
        String reasonMsg = "Reason";
        AgreementEntity pendingAgreement = createPendingAgreement().getAgreementEntity();
        AgreementEntity rejectAgreement = backofficeAgreementService.rejectAgreement(pendingAgreement.getId(),
                                                                                     reasonMsg);
        Assertions.assertEquals(AgreementStateEnum.REJECTED, rejectAgreement.getState());
        Assertions.assertNull(rejectAgreement.getStartDate());
        Assertions.assertNull(rejectAgreement.getEndDate());
        Assertions.assertEquals(reasonMsg, rejectAgreement.getRejectReasonMessage());
    }

    @Test
    void RejectAgreement_RejectAgreementWithDraftStatus_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        //agreement state not PENDING
        final String agreementId = agreementEntity.getId();
        String reasonMsg = "Reason";
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> backofficeAgreementService.rejectAgreement(agreementId, reasonMsg));
        agreementEntity = agreementService.findAgreementById(agreementId);
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());

    }

    @Test
    void SuspendDiscount_SuspendDiscount_Ok() {
        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        DiscountEntity discountEntity = testObject.getDiscountEntityList().get(0);

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        String reasonMsg = "reasonMessage";
        discountEntity = discountService.suspendDiscount(agreementEntity.getId(), discountEntity.getId(), reasonMsg);
        Assertions.assertEquals(DiscountStateEnum.SUSPENDED, discountEntity.getState());
        Assertions.assertEquals(reasonMsg, discountEntity.getSuspendedReasonMessage());
        Assertions.assertNotNull(agreementEntity.getEntityType());
    }


    @Test
    void SuspendDiscount_SuspendDiscountOfNotPublicDiscount_ThrowException() {
        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        DiscountEntity discountEntity = testObject.getDiscountEntityList().get(0);
        String reasonMsg = "reasonMessage";
        var agreementId = agreementEntity.getId();
        var discountId = discountEntity.getId();

        Assertions.assertThrows(InvalidRequestException.class,
                                () -> discountService.suspendDiscount(agreementId, discountId, reasonMsg));
        Assertions.assertEquals(DiscountStateEnum.DRAFT, discountEntity.getState());
        Assertions.assertNull(discountEntity.getSuspendedReasonMessage());
    }
}
