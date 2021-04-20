package it.gov.pagopa.service;

import it.gov.pagopa.cgn.IntegrationAbstractTest;
import it.gov.pagopa.cgn.TestUtils;
import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.AddressEntity;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.repository.AddressRepository;
import it.gov.pagopa.repository.AgreementRepository;
import it.gov.pagopa.repository.AgreementUserRepository;
import it.gov.pagopa.repository.ProfileRepository;
import org.junit.jupiter.api.AfterEach;
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
class ProfileServiceTest extends IntegrationAbstractTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private AgreementUserRepository userRepository;

    private AgreementEntity agreementEntity;

    @BeforeEach
    void beforeEach() {
        agreementEntity = agreementService.createAgreementIfNotExists();
    }
    @AfterEach
    void clean() {
        profileRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void Create_CreateProfileOnlineWithValidData_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, agreementEntity.getId());
        Assertions.assertNotNull(profileDB.getId());
        Assertions.assertNotNull(profileDB.getReferent());
        Assertions.assertNotNull(profileDB.getReferent().getId());
        Assertions.assertNull(profileDB.getAddressList());
        Assertions.assertEquals(SalesChannelEnum.ONLINE, profileDB.getSalesChannel());

    }

    @Test
    void Create_CreateProfilePhysicalWithValidData_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, agreementEntity.getId());

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
    void Create_CreateProfileBothWithValidData_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, agreementEntity.getId());

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
        profileService.createRegistry(profileEntity, agreementEntity.getId());

        ProfileEntity profileEntity2 = TestUtils.createSampleProfileWithCommonFields();
        profileEntity2.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity2.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(InvalidRequestException.class, () -> profileService.createRegistry(profileEntity2, agreementEntity.getId()));
    }

    @Test
    void Create_CreateProfileWithInvalidWebsiteUrl_ThrowException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("pagopa.gov.it");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createRegistry(profileEntity, agreementEntity.getId());

        });
    }

    @Test
    void Create_CreateProfileWithInvalidEmail_ThrowException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setPecAddress("fakeemail.it");
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createRegistry(profileEntity, agreementEntity.getId());
        });
    }

    @Test
    void Create_CreateProfileAndCheckSubscriptionRegistryDate_DateUpdated() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, agreementEntity.getId());
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertNotNull(profileDB.getAgreement().getProfileModifiedDate());
        Assertions.assertEquals(LocalDate.now(), profileDB.getAgreement().getProfileModifiedDate());
    }

    @Test
    void Update_UpdateOnlineProfileWithSameSalesChannel_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileService.createRegistry(profileEntity, agreementEntity.getId());
        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setSalesChannel(SalesChannelEnum.ONLINE);
        ProfileEntity profileDB = profileService.updateProfile(agreementEntity.getId(), toUpdateProfile);
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertEquals(toUpdateProfile.getName(), profileDB.getName());
        Assertions.assertEquals(toUpdateProfile.getWebsiteUrl(), profileDB.getWebsiteUrl());
        Assertions.assertTrue(CollectionUtils.isEmpty(profileDB.getAddressList()));
    }

    @Test
    void Update_UpdateOfflineProfileWithSameSalesChannel_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.createRegistry(profileEntity, agreementEntity.getId());
        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAddressList(TestUtils.createSampleAddress(profileEntity));
        toUpdateProfile.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.updateProfile(agreementEntity.getId(), toUpdateProfile);
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertEquals(toUpdateProfile.getName(), profileDB.getName());
        Assertions.assertEquals(toUpdateProfile.getWebsiteUrl(), profileDB.getWebsiteUrl());
        Assertions.assertNotNull(profileDB.getAddressList());
        List<AddressEntity> addresses = addressRepository.findByProfileId(profileDB.getId());
        Assertions.assertEquals(profileDB.getAddressList().size(), addresses.size());
    }

    @Test
    void Update_UpdateOfflineProfileWithDifferentSalesChannel_Ok() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileService.createRegistry(profileEntity, agreementEntity.getId());
        ProfileEntity toUpdateProfile = TestUtils.createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAddressList(TestUtils.createSampleAddress(profileEntity));
        toUpdateProfile.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.updateProfile(agreementEntity.getId(), toUpdateProfile);
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertEquals(toUpdateProfile.getName(), profileDB.getName());
        Assertions.assertEquals(toUpdateProfile.getWebsiteUrl(), profileDB.getWebsiteUrl());
        Assertions.assertNotNull(profileDB.getAddressList());
        List<AddressEntity> addresses = addressRepository.findByProfileId(profileDB.getId());
        Assertions.assertEquals(profileDB.getAddressList().size(), addresses.size());
    }

}

