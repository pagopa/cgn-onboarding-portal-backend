package it.gov.pagopa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.cgnonboardingportal.model.Address;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import it.gov.pagopa.enums.DiscountStateEnum;
import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.model.*;
import it.gov.pagopa.repository.AgreementRepository;
import it.gov.pagopa.repository.AgreementUserRepository;
import it.gov.pagopa.repository.DiscountRepository;
import it.gov.pagopa.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class BaseTest {

    protected static final String AGREEMENTS_CONTROLLER_PATH = "/agreements/";

    @Autowired
    protected AgreementUserRepository userRepository;

    @Autowired
    protected AgreementRepository agreementRepository;

    @Autowired
    protected DiscountRepository discountRepository;

    @Autowired
    protected ProfileRepository profileRepository;

   // @AfterAll
    public void afterAll() {
        log.info("AfterAll-->");
        discountRepository.deleteAll();
        profileRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
        log.info("<--AfterAll");
    }

    protected String getProfilePath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH + agreementId + "/profile";
    }

    protected String getDiscountPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH + agreementId + "/discounts";
    }

    protected ReferentEntity createSampleReferent(ProfileEntity profileEntity) {
        ReferentEntity referentEntity = new ReferentEntity();
        referentEntity.setFirstName("FIRST_NAME");
        referentEntity.setLastName("LAST_NAME");
        referentEntity.setEmailAddress("referent.registry@pagopa.it");
        referentEntity.setTelephoneNumber("+390123456789");
        referentEntity.setProfile(profileEntity);
        return referentEntity;
    }

    protected ProfileEntity createSampleProfileWithCommonFields() {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setFullName("FULL_NAME");
        profileEntity.setName("NAME");
        profileEntity.setPecAddress("pec.address@pagopa.it");
        profileEntity.setDescription("A Description");
        profileEntity.setReferent(createSampleReferent(profileEntity));
        return profileEntity;
    }

    protected List<AddressEntity> createSampleAddress(ProfileEntity profileEntity) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setProfile(profileEntity);
        addressEntity.setStreet("GARIBALDI 1");
        addressEntity.setCity("ROME");
        addressEntity.setDistrict("RM");
        addressEntity.setZipCode("00100");
        addressEntity.setLatitude(42.92439);
        addressEntity.setLongitude(12.50181);
        List<AddressEntity> list = new ArrayList<>(1);
        list.add(addressEntity);
        return list;
    }

    protected List<Address> createSampleAddressDto() {
        Address address = new Address();
        address.setStreet("GARIBALDI 1");
        address.setCity("ROME");
        address.setDistrict("RM");
        address.setZipCode("00100");
        return Collections.singletonList(address);
    }

    protected ProfileEntity createSampleProfileEntity(AgreementEntity agreementEntity) {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setAgreement(agreementEntity);
        return profileEntity;
    }

    protected UpdateProfile createSampleUpdateProfileWithCommonFields() {
        UpdateProfile profileDto = new UpdateProfile();
        profileDto.setName("name_dto");
        profileDto.setDescription("description_dto");
        profileDto.setPecAddress("myname.profile@pagopa.it");
        UpdateReferent updateReferent = new UpdateReferent();
        updateReferent.setFirstName("referent_first_name");
        updateReferent.setLastName("referent_last_name");
        updateReferent.setEmailAddress("referent.profile@pagopa.it");
        updateReferent.setTelephoneNumber("01234567");
        profileDto.setReferent(updateReferent);
        return profileDto;
    }

    protected DiscountEntity createSampleDiscountEntity(AgreementEntity agreement) {
        DiscountEntity discountEntity = new DiscountEntity();
        discountEntity.setState(DiscountStateEnum.DRAFT);
        discountEntity.setName("discount_name");
        discountEntity.setDescription("discount_description");
        discountEntity.setDiscountValue(15.99);
        discountEntity.setCondition("discount_condition");
        discountEntity.setStartDate(LocalDate.now());
        discountEntity.setEndDate(LocalDate.now().plusMonths(6));
        discountEntity.setStaticCode("discount_static_code");
        discountEntity.setAgreement(agreement);
        discountEntity.setProducts(getProductEntityList(discountEntity));
        return discountEntity;
    }

    protected List<DiscountProductEntity> getProductEntityList(DiscountEntity discountEntity) {
        List<DiscountProductEntity> productEntityList = new ArrayList<>();
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory("VIAGGI");
        productEntityList.add(productEntity);
        productEntity = new DiscountProductEntity();
        productEntity.setProductCategory("SPORT");
        productEntityList.add(productEntity);
        productEntityList.forEach(p -> p.setDiscount(discountEntity));
        return productEntityList;
    }

    protected String getJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper.writeValueAsString(obj);
    }



}
