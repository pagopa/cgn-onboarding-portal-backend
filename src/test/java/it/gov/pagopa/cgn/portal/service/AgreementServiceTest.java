package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
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
    private DocumentService documentService;

    @Test
    void Create_CreateAgreementWithInitializedData_Ok() {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        Assertions.assertNotNull(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getImageUrl());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());
    }

    @Test
    void Create_CreatedAgreementWithValidId_Ok() {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        Optional<AgreementUserEntity> userEntityOptional;
        userEntityOptional = this.userRepository.findAll().stream()
                .filter((user) -> user.getAgreementId().equals(agreementEntity.getId())).findFirst();
        Assertions.assertTrue(userEntityOptional.isPresent());
    }

    @Test
    void Create_CreateMultipleAgreement_CreatedOnlyOneAgreement() {
        AgreementEntity userCreated1 = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        AgreementEntity userCreated2 = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        Assertions.assertEquals(userCreated1, userCreated2);
    }

    @Test
    void RequestApproval_RequestApprovalWithAllRequiredData_Ok() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        Assertions.assertDoesNotThrow(() -> agreementService.requestApproval(agreementEntity.getId()));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.PENDING, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
        Assertions.assertNotNull(pendingAgreement.getRequestApprovalTime());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutDiscount_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        var agreementId = agreementEntity.getId();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        saveSampleDocuments(agreementEntity);
        Assertions.assertThrows(InvalidRequestException.class, () -> agreementService.requestApproval(agreementId));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
        Assertions.assertNull(pendingAgreement.getRequestApprovalTime());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutProfile_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        var agreementId = agreementEntity.getId();

        saveSampleDocuments(agreementEntity);

        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        Assertions.assertThrows(InvalidRequestException.class, () -> discountService.createDiscount(agreementId, discountEntity));
        Assertions.assertThrows(InvalidRequestException.class, () -> agreementService.requestApproval(agreementId));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
        Assertions.assertNull(pendingAgreement.getRequestApprovalTime());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutDocuments_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        var agreementId = agreementEntity.getId();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        Assertions.assertThrows(InvalidRequestException.class, () -> agreementService.requestApproval(agreementId));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
    }

    @Test
    void RequestApproval_RequestApprovalWithoutOneDocument_ThrowException() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        var agreementId = agreementEntity.getId();
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DocumentEntity documentEntity = TestUtils.createDocument(agreementEntity, DocumentTypeEnum.AGREEMENT);
        documentRepository.save(documentEntity);
        Assertions.assertThrows(InvalidRequestException.class, () -> agreementService.requestApproval(agreementId));
        AgreementEntity pendingAgreement = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, pendingAgreement.getState());
        Assertions.assertNull(pendingAgreement.getStartDate());
        Assertions.assertNull(pendingAgreement.getEndDate());
        Assertions.assertNull(pendingAgreement.getImageUrl());
        Assertions.assertNull(pendingAgreement.getRejectReasonMessage());
    }

}
