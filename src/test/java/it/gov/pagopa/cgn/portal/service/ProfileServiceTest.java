package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.AddressRepository;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
class ProfileServiceTest
        extends IntegrationAbstractTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private TestReferentRepository referentRepository;

    @Autowired
    private BackofficeAgreementService backofficeAgreementService;

    private AgreementEntity agreementEntity;

    private String agreementId;

    @BeforeEach
    void init() {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                      EntityType.PRIVATE,
                                                                      TestUtils.FAKE_ORGANIZATION_NAME);
        agreementId = agreementEntity.getId();
    }

    @Test
    void Create_CreateProfileOnlineWithValidData_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        ProfileEntity profileDB = profileService.createProfile(profileEntity, agreementId);
        Assertions.assertNotNull(profileDB.getId());
        Assertions.assertNotNull(profileDB.getReferent());
        Assertions.assertNotNull(profileDB.getReferent().getId());
        Assertions.assertNull(profileDB.getAddressList());
        Assertions.assertEquals(SalesChannelEnum.ONLINE, profileDB.getSalesChannel());
        Assertions.assertEquals(profileEntity.getLegalRepresentativeTaxCode(),
                                profileDB.getLegalRepresentativeTaxCode());
        Assertions.assertEquals(profileEntity.getLegalRepresentativeFullName(),
                                profileDB.getLegalRepresentativeFullName());
        Assertions.assertEquals(profileEntity.getLegalOffice(), profileDB.getLegalOffice());
        Assertions.assertEquals(profileEntity.getDiscountCodeType(), profileDB.getDiscountCodeType());
        Assertions.assertEquals(profileEntity.getTelephoneNumber(), profileDB.getTelephoneNumber());
    }

    @Test
    void Create_CreateProfilePhysicalWithValidData_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.createProfile(profileEntity, agreementId);

        Assertions.assertNotNull(profileDB.getId());
        Assertions.assertNotNull(profileDB.getReferent());
        Assertions.assertNotNull(profileDB.getReferent().getId());
        Assertions.assertNotNull(profileDB.getAddressList());
        Assertions.assertFalse(profileDB.getAddressList().isEmpty());
        Assertions.assertNotNull(profileDB.getAddressList().get(0));
        Assertions.assertNotNull(profileDB.getAddressList().get(0).getId());
        Assertions.assertEquals(SalesChannelEnum.OFFLINE, profileDB.getSalesChannel());
        Assertions.assertNull(profileDB.getWebsiteUrl());
    }

    @Test
    void Create_CreateProfileWithoutEnAndDeNamesIfItIsGiven_ThrowsException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setNameEn(null);
        profileEntity.setNameDe(null);
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> profileService.createProfile(profileEntity, agreementId));
    }

    @Test
    void Create_CreateProfilePhysicalWithoutAddresses_ThrowsException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> profileService.createProfile(profileEntity, agreementId));
    }

    @Test
    void Create_CreateProfileBothWithValidData_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);
        ProfileEntity profileDB = profileService.createProfile(profileEntity, agreementId);

        Assertions.assertNotNull(profileDB.getId());
        Assertions.assertNotNull(profileDB.getReferent());
        Assertions.assertNotNull(profileDB.getReferent().getId());
        Assertions.assertNotNull(profileDB.getAddressList());
        Assertions.assertFalse(profileDB.getAddressList().isEmpty());
        Assertions.assertNotNull(profileDB.getAddressList().get(0));
        Assertions.assertNotNull(profileDB.getAddressList().get(0).getId());
        Assertions.assertEquals(SalesChannelEnum.BOTH, profileDB.getSalesChannel());
        Assertions.assertNotNull(profileDB.getWebsiteUrl());
    }

    @Test
    void Create_CreateProfileMultipleTimes_ThrowException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);
        profileService.createProfile(profileEntity, agreementId);

        ProfileEntity profileEntity2 = TestUtils.createSampleProfileWithCommonFields();
        profileEntity2.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity2.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> profileService.createProfile(profileEntity2, agreementId));
    }

    @Test
    void Create_CreateProfileWithInvalidEmail_ThrowException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setPecAddress("fakeemail.it");
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createProfile(profileEntity, agreementId);
        });
    }

    @Test
    void Update_UpdateOnlineProfileWithSameSalesChannel_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileService.createProfile(profileEntity, agreementId);
        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setSalesChannel(SalesChannelEnum.ONLINE);
        ProfileEntity profileDB = profileService.updateProfile(agreementId, toUpdateProfile);
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertEquals(toUpdateProfile.getName(), profileDB.getName());
        Assertions.assertEquals(toUpdateProfile.getWebsiteUrl(), profileDB.getWebsiteUrl());
        Assertions.assertTrue(CollectionUtils.isEmpty(profileDB.getAddressList()));
    }

    @Test
    void Update_UpdateOfflineProfileWithSameSalesChannel_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.createProfile(profileEntity, agreementId);
        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAllNationalAddresses(false);
        toUpdateProfile.setAddressList(TestUtils.createSampleAddress(profileEntity));
        toUpdateProfile.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.updateProfile(agreementId, toUpdateProfile);
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertEquals(toUpdateProfile.getName(), profileDB.getName());
        Assertions.assertEquals(toUpdateProfile.getWebsiteUrl(), profileDB.getWebsiteUrl());
        Assertions.assertNotNull(profileDB.getAddressList());
        List<AddressEntity> addresses = addressRepository.findByProfileId(profileDB.getId());
        Assertions.assertEquals(profileDB.getAddressList().size(), addresses.size());
    }

    @Test
    void Update_UpdateOfflineProfileRemovingAddresses_ShouldSetAllNationalAddresses() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.createProfile(profileEntity, agreementId);

        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAllNationalAddresses(false);
        toUpdateProfile.setSalesChannel(SalesChannelEnum.OFFLINE);

        ProfileEntity profileDB = profileService.updateProfile(agreementId, toUpdateProfile);
        Assertions.assertTrue(profileDB.getAllNationalAddresses());
    }

    @Test
    void Update_UpdateOfflineProfileWithDifferentSalesChannel_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileService.createProfile(profileEntity, agreementId);
        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAllNationalAddresses(false);
        toUpdateProfile.setAddressList(TestUtils.createSampleAddress(profileEntity));
        toUpdateProfile.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.updateProfile(agreementId, toUpdateProfile);
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertEquals(toUpdateProfile.getName(), profileDB.getName());
        Assertions.assertEquals(toUpdateProfile.getWebsiteUrl(), profileDB.getWebsiteUrl());
        Assertions.assertNotNull(profileDB.getAddressList());
        List<AddressEntity> addresses = addressRepository.findByProfileId(profileDB.getId());
        Assertions.assertEquals(profileDB.getAddressList().size(), addresses.size());
    }

    @Test
    void Update_UpdateApprovedAgreementUpdateLastModifyDate_Ok() {
        setAdminAuth();
        final String legalOffice = "new_legalOffice";
        AgreementEntity agreement = createPendingAgreement().getAgreementEntity();
        agreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementRepository.save(agreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreement));
        agreement = backofficeAgreementService.approveAgreement(agreement.getId());
        ProfileEntity profileEntity = profileService.getProfile(agreement.getId()).orElseThrow();
        profileEntity.setLegalOffice(legalOffice);
        //added to avoid LazyInitializationException

        profileEntity.setReferent(referentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setSecondaryReferentList(secondaryReferentRepository.findByProfileId(profileEntity.getId()));

        profileEntity = profileService.updateProfile(agreement.getId(), profileEntity);
        Assertions.assertEquals(legalOffice, profileEntity.getLegalOffice());
        agreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.now(), agreement.getInformationLastUpdateDate());

    }

    @Test
    void Update_UpdatePendingAgreementNotUpdateLastModifyDate_Ok() {
        final String legalOffice = "new_legalOffice";
        AgreementTestObject agreementTestObject = createPendingAgreement();
        AgreementEntity agreement = agreementTestObject.getAgreementEntity();
        ProfileEntity profileEntity = agreementTestObject.getProfileEntity();
        profileEntity.setLegalOffice(legalOffice);
        profileEntity = profileService.updateProfile(agreement.getId(), profileEntity);
        Assertions.assertEquals(legalOffice, profileEntity.getLegalOffice());
        agreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertNull(agreement.getInformationLastUpdateDate());

    }

    @Test
    void Update_UpdateProfileOfRejectedAgreement_StateAgreementUpdateToDraft() {
        setAdminAuth();
        final String legalOffice = "new_legalOffice";
        AgreementEntity agreement = createPendingAgreement().getAgreementEntity();
        agreement.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementRepository.save(agreement);
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreementEntity));
        agreement = backofficeAgreementService.rejectAgreement(agreement.getId(), "a reason message");

        ProfileEntity profileEntity = profileService.getProfile(agreement.getId()).orElseThrow();
        profileEntity.setLegalOffice(legalOffice);
        //added to avoid LazyInitializationException
        profileEntity.setReferent(referentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setSecondaryReferentList(secondaryReferentRepository.findByProfileId(profileEntity.getId()));

        profileEntity = profileService.updateProfile(agreement.getId(), profileEntity);
        Assertions.assertEquals(legalOffice, profileEntity.getLegalOffice());
        agreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreement.getState());
        Assertions.assertNull(agreement.getStartDate());
        Assertions.assertNull(agreement.getEndDate());
        Assertions.assertNull(agreement.getRejectReasonMessage());
        Assertions.assertNull(agreement.getRequestApprovalTime());
        Assertions.assertNull(agreement.getBackofficeAssignee());
        List<DocumentEntity> documents = documentRepository.findByAgreementId(agreement.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));
    }

    @Test
    void Update_UpdateProfileWithDocumentUploadedWillDeleteDocuments_Ok() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                           EntityType.PRIVATE,
                                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementId);
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementId, discountEntity);
        saveSampleDocuments(agreementEntity);
        Assertions.assertEquals(2, documentRepository.findByAgreementId(agreementId).size());

        ProfileEntity updatedProfile = TestUtils.createSampleProfileEntity(agreementEntity);
        updatedProfile.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.updateProfile(agreementId, updatedProfile);

        Assertions.assertEquals(0, documentRepository.findByAgreementId(agreementId).size());
    }
}
