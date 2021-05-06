package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles({"dev"})
class BackofficeAgreementServiceTest extends IntegrationAbstractTest {

    @Autowired
    private BackofficeAgreementService backofficeAgreementService;

    @Test
    void GetAgreement_GetAgreementWithoutFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder().build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));
    }

    @Test
    void GetAgreement_GetAgreementWithProfileFullNameFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .profileFullName(pendingAgreement.getProfile().getFullName()).build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));
    }

    @Test
    void GetAgreement_GetAgreementWithProfileFullNameLowerCaseFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .profileFullName(pendingAgreement.getProfile().getFullName().toLowerCase()).build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateFromFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .requestDateFrom(LocalDate.now().minusDays(2)).build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateToFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .requestDateTo(LocalDate.now().plusMonths(1)).build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateFromAndToFilter_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .requestDateFrom(LocalDate.now().minusDays(2))
                .requestDateTo(LocalDate.now().plusMonths(1))
                .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));
    }

    @Test
    void GetAgreement_GetAgreementWithRequestDateFuture_AgreementNotFound() {
        createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .requestDateFrom(LocalDate.now().plusDays(2))
                .requestDateTo(LocalDate.now().plusMonths(1))
                .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(0L, page.getTotalElements());
        Assertions.assertEquals(0, page.getTotalPages());
        Assertions.assertTrue(CollectionUtils.isEmpty(page.getContent()));

    }

    @Test
    void GetAgreement_GetAgreementWithAssignedStatus_AgreementFound() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .agreementState(AgreementState.ASSIGNEDAGREEMENT.getValue())
                .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        Assertions.assertEquals(pendingAgreement, page.getContent().get(0));

    }

    @Test
    void GetAgreement_GetAgreementWithApprovedStatus_AgreementNotFound() {
        createPendingAgreement();
        BackofficeFilter filter = BackofficeFilter.builder()
                .agreementState(AgreementState.APPROVEDAGREEMENT.getValue())
                .build();
        Page<AgreementEntity> page = backofficeAgreementService.getAgreements(filter);
        Assertions.assertEquals(0L, page.getTotalElements());
        Assertions.assertEquals(0, page.getTotalPages());
        Assertions.assertTrue(CollectionUtils.isEmpty(page.getContent()));

    }

    @Test
    void AssignAgreement_AssignAgreement_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        AgreementEntity agreementEntity = backofficeAgreementService.assignAgreement(pendingAgreement.getId());
        Assertions.assertTrue(StringUtils.isNotBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());

    }

    @Test
    void AssignAgreement_AssignAgreementMultipleTimes_ThrowException() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        AgreementEntity agreementEntity = backofficeAgreementService.assignAgreement(pendingAgreement.getId());

        Assertions.assertTrue(StringUtils.isNotBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
        Assertions.assertThrows(InvalidRequestException.class,
                () ->backofficeAgreementService.assignAgreement(pendingAgreement.getId()));

    }

    @Test
    void AssignAgreement_AssignAgreementWithStatusDraft_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        //agreement state not PENDING

        Assertions.assertThrows(InvalidRequestException.class,
                () ->backofficeAgreementService.assignAgreement(agreementEntity.getId()));
    }

    @Test
    void UnassignAgreement_UnassignAgreement_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        backofficeAgreementService.assignAgreement(pendingAgreement.getId());

        AgreementEntity agreementEntity = backofficeAgreementService.unassignAgreement(pendingAgreement.getId());
        Assertions.assertTrue(StringUtils.isBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
    }

    @Test
    void UnassignAgreement_UnassignAgreementWithoutAssignment_ThrowException() {
        AgreementEntity pendingAgreement = createPendingAgreement();

        Assertions.assertThrows(InvalidRequestException.class,
                () ->backofficeAgreementService.unassignAgreement(pendingAgreement.getId()));
        AgreementEntity agreementEntity = agreementService.findById(pendingAgreement.getId());
        Assertions.assertTrue(StringUtils.isBlank(agreementEntity.getBackofficeAssignee()));
        Assertions.assertEquals(AgreementStateEnum.PENDING, agreementEntity.getState());
    }

    @Test
    void ApproveAgreement_ApproveAgreement_Ok() {
        AgreementEntity pendingAgreement = createPendingAgreement();
        pendingAgreement.setBackofficeAssignee(BackofficeAgreementService.FAKE_BACKOFFICE_ID);
        pendingAgreement = agreementRepository.save(pendingAgreement);
        AgreementEntity approveAgreement = backofficeAgreementService.approveAgreement(pendingAgreement.getId());
        Assertions.assertEquals(AgreementStateEnum.APPROVED, approveAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), approveAgreement.getStartDate());
        Assertions.assertEquals(CGNUtils.getDefaultAgreementEndDate(), approveAgreement.getEndDate());
        Assertions.assertNull(approveAgreement.getRejectReasonMessage());

    }

    @Test
    void ApproveAgreement_ApproveAgreementWithDraftStatus_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        //agreement state not PENDING
        final String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                ()-> backofficeAgreementService.approveAgreement(agreementId));
        agreementEntity = agreementService.findById(agreementEntity.getId());

        Assertions.assertNotEquals(AgreementStateEnum.APPROVED, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());

    }

    @Test
    void RejectAgreement_RejectAgreement_Ok() {
        String reasonMsg = "Reason";
        AgreementEntity pendingAgreement = createPendingAgreement();
        AgreementEntity rejectAgreement = backofficeAgreementService.rejectAgreement(
                pendingAgreement.getId(), reasonMsg);
        Assertions.assertEquals(AgreementStateEnum.REJECTED, rejectAgreement.getState());
        Assertions.assertNull(rejectAgreement.getStartDate());
        Assertions.assertNull(rejectAgreement.getEndDate());
        Assertions.assertEquals(reasonMsg, rejectAgreement.getRejectReasonMessage());
    }

    @Test
    void RejectAgreement_RejectAgreementWithDraftStatus_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        //agreement state not PENDING
        final String agreementId = agreementEntity.getId();
        String reasonMsg = "Reason";
        Assertions.assertThrows(InvalidRequestException.class,
                ()-> backofficeAgreementService.rejectAgreement(agreementId, reasonMsg));
        agreementEntity = agreementService.findById(agreementId);
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());

    }

}
