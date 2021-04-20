package it.gov.pagopa.service;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.AddressEntity;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.repository.AddressRepository;
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
class ProfileServiceTest extends BaseTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private AddressRepository addressRepository;

    private AgreementEntity agreementEntity;

    @BeforeEach
    void beforeEach() {
        agreementEntity = agreementService.createAgreementIfNotExists();
        profileRepository.deleteAll();
    }

    @Test
    void Create_CreateProfileOnlineWithValidData_Ok() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
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
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setAddressList(createSampleAddress(profileEntity));
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
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setAddressList(createSampleAddress(profileEntity));
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
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setAddressList(createSampleAddress(profileEntity));
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);
        profileService.createRegistry(profileEntity, agreementEntity.getId());

        ProfileEntity profileEntity2 = createSampleProfileWithCommonFields();
        profileEntity2.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity2.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(InvalidRequestException.class, () -> profileService.createRegistry(profileEntity2, agreementEntity.getId()));
    }

    @Test
    void Create_CreateProfileWithInvalidWebsiteUrl_ThrowException() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("pagopa.gov.it");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createRegistry(profileEntity, agreementEntity.getId());

        });
    }

    @Test
    void Create_CreateProfileWithInvalidEmail_ThrowException() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setPecAddress("fakeemail.it");
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createRegistry(profileEntity, agreementEntity.getId());
        });
    }

    @Test
    void Create_CreateProfileAndCheckSubscriptionRegistryDate_DateUpdated() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setAddressList(createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, agreementEntity.getId());
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertNotNull(profileDB.getAgreement().getProfileModifiedDate());
        Assertions.assertEquals(LocalDate.now(), profileDB.getAgreement().getProfileModifiedDate());
    }

    @Test
    void Update_UpdateOnlineProfileWithSameSalesChannel_Ok() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileService.createRegistry(profileEntity, agreementEntity.getId());
        ProfileEntity toUpdateProfile = createSampleProfileWithCommonFields();
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
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setAddressList(createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileService.createRegistry(profileEntity, agreementEntity.getId());
        ProfileEntity toUpdateProfile = createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAddressList(createSampleAddress(profileEntity));
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
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileService.createRegistry(profileEntity, agreementEntity.getId());
        ProfileEntity toUpdateProfile = createSampleProfileWithCommonFields();
        toUpdateProfile.setName("updated_name");
        toUpdateProfile.setWebsiteUrl("https://www.pagopa.gov.it/test");
        toUpdateProfile.setAddressList(createSampleAddress(profileEntity));
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

