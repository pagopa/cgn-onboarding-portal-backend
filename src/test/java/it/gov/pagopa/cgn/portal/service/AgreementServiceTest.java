package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementServiceTest extends IntegrationAbstractTest {
    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DiscountService discountService;

    @Test
    void Create_CreateAgreementWithInitializedData_Ok() {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        Assertions.assertNotNull(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getImageUrl());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());
    }

    @Test
    void Create_CreatedAgreementWithValidId_Ok() {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        Optional<AgreementUserEntity> userEntityOptional;
        userEntityOptional = this.userRepository.findAll().stream()
                .filter((user) -> user.getAgreementId().equals(agreementEntity.getId())).findFirst();
        Assertions.assertTrue(userEntityOptional.isPresent());
    }

    @Test
    void Create_CreateMultipleAgreement_CreatedOnlyOneAgreement() {
        AgreementEntity userCreated1 = this.agreementService.createAgreementIfNotExists();
        AgreementEntity userCreated2 = this.agreementService.createAgreementIfNotExists();
        Assertions.assertEquals(userCreated1, userCreated2);
    }

    @Test
    void RequestApproval_RequestApprovalWithAllRequiredData_Ok() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        //todo add documents when will be implemented
        Assertions.assertDoesNotThrow(() -> agreementService.requestApproval(agreementEntity.getId()));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.PENDING, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutDiscount_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        Assertions.assertThrows(InvalidRequestException.class, () -> agreementService.requestApproval(agreementEntity.getId()));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutProfile_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();

        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        Assertions.assertThrows(InvalidRequestException.class, () -> discountService.createDiscount(agreementEntity.getId(), discountEntity));
        Assertions.assertThrows(InvalidRequestException.class, () -> agreementService.requestApproval(agreementEntity.getId()));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
    }
}
