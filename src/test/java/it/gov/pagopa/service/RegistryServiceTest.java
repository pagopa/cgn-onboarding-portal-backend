package it.gov.pagopa.service;

import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.*;
import it.gov.pagopa.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
@Transactional
class RegistryServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SessionFactory sessionFactory;

    private AgreementEntity subscription;


    @BeforeEach
    void beforeEach() {
        subscription = agreementService.createAgreementIfNotExists();
    }

    @Test
    void Create_CreateProfileOnlineWithValidData_Ok() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, subscription.getId());
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
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, subscription.getId());

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
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, subscription.getId());

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
        profileService.createRegistry(profileEntity, subscription.getId());

        ProfileEntity profileEntity2 = createSampleProfileWithCommonFields();
        profileEntity2.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity2.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(InvalidRequestException.class, () -> profileService.createRegistry(profileEntity2, subscription.getId()));
    }

    @Test
    void Create_CreateProfileWithInvalidWebsiteUrl_ThrowException() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("pagopa.gov.it");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createRegistry(profileEntity, subscription.getId());
            sessionFactory.getCurrentSession().flush();

        });
    }

    @Test
    void Create_CreateProfileWithInvalidEmail_ThrowException() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setPecAddress("fakeemail.it");
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        Assertions.assertThrows(Exception.class, () -> {
            profileService.createRegistry(profileEntity, subscription.getId());
            sessionFactory.getCurrentSession().flush();
        });
    }

    @Test
    void Create_CreateProfileAndCheckSubscriptionRegistryDate_DateUpdated() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setAddressList(createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        ProfileEntity profileDB = profileService.createRegistry(profileEntity, subscription.getId());
        Assertions.assertNotNull(profileDB);
        Assertions.assertNotNull(profileDB.getAgreement());
        Assertions.assertNotNull(profileDB.getAgreement().getProfileModifiedDate());
        Assertions.assertEquals(LocalDate.now(), profileDB.getAgreement().getProfileModifiedDate());
    }


    private ReferentEntity createSampleReferent(ProfileEntity profileEntity) {
        ReferentEntity referentEntity = new ReferentEntity();
        referentEntity.setFirstName("FIRST_NAME");
        referentEntity.setLastName("LAST_NAME");
        referentEntity.setEmailAddress("referent.registry@pagopa.it");
        referentEntity.setTelephoneNumber("+390123456789");
        referentEntity.setProfile(profileEntity);
        return referentEntity;
    }

    private ProfileEntity createSampleProfileWithCommonFields() {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setFullName("FULL_NAME");
        profileEntity.setName("NAME");
        profileEntity.setPecAddress("pec.address@pagopa.it");
        profileEntity.setDescription("A Description");
        profileEntity.setReferent(createSampleReferent(profileEntity));
        return profileEntity;
    }

    private List<AddressEntity> createSampleAddress(ProfileEntity profileEntity) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setProfile(profileEntity);
        addressEntity.setStreet("GARIBALDI 1");
        addressEntity.setCity("ROME");
        addressEntity.setDistrict("RM");
        addressEntity.setZipCode("00100");
        addressEntity.setLatitude(42.92439);
        addressEntity.setLongitude(12.50181);
        return Collections.singletonList(addressEntity);
    }

}

