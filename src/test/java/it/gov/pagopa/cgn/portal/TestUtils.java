package it.gov.pagopa.cgn.portal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgnonboardingportal.model.Address;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestUtils {
    public static final String AGREEMENTS_CONTROLLER_PATH = "/agreements/";
    public static final String BACKOFFICE_CONTROLLER_PATH = "/backoffice/";

    public static String getProfilePath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH + agreementId + "/profile";
    }

    public static String getDiscountPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH + agreementId + "/discounts";
    }

    public static String getDiscountPublishingPath(String agreementId, Long discountId) {
        return getDiscountPath(agreementId) + "/" + discountId + "/publishing";
    }

    public static String getDocumentPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH + agreementId + "/documents";
    }

    public static String getAgreementApprovalPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH + agreementId + "/approval";
    }


    public static ReferentEntity createSampleReferent(ProfileEntity profileEntity) {
        ReferentEntity referentEntity = new ReferentEntity();
        referentEntity.setFirstName("FIRST_NAME");
        referentEntity.setLastName("LAST_NAME");
        referentEntity.setEmailAddress("referent.registry@pagopa.it");
        referentEntity.setTelephoneNumber("+390123456789");
        referentEntity.setProfile(profileEntity);
        referentEntity.setRole("CEO");
        return referentEntity;
    }

    public static ProfileEntity createSampleProfileWithCommonFields() {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setFullName("FULL_NAME");
        profileEntity.setName("NAME");
        profileEntity.setTaxCodeOrVat("abcdeghilmnopqrs");
        profileEntity.setPecAddress("pec.address@pagopa.it");
        profileEntity.setDescription("A Description");
        profileEntity.setReferent(createSampleReferent(profileEntity));
        profileEntity.setLegalRepresentativeTaxCode("abcdeghilmnopqrs");
        profileEntity.setLegalRepresentativeFullName("full name");
        profileEntity.setLegalOffice("legal office");
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.STATIC);
        profileEntity.setTelephoneNumber("12345678");
        return profileEntity;
    }

    public static List<AddressEntity> createSampleAddress(ProfileEntity profileEntity) {
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

    public static List<Address> createSampleAddressDto() {
        Address address = new Address();
        address.setStreet("GARIBALDI 1");
        address.setCity("ROME");
        address.setDistrict("RM");
        address.setZipCode("00100");
        return Collections.singletonList(address);
    }

    public static ProfileEntity createSampleProfileEntity(AgreementEntity agreementEntity) {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);
        profileEntity.setAgreement(agreementEntity);
        return profileEntity;
    }

    public static UpdateProfile createSampleUpdateProfileWithCommonFields() {
        UpdateProfile profileDto = new UpdateProfile();
        profileDto.setName("name_dto");
        profileDto.setDescription("description_dto");
        profileDto.setPecAddress("myname.profile@pagopa.it");
        profileDto.setLegalRepresentativeTaxCode("abcdeghilmnopqrs");
        profileDto.setLegalRepresentativeFullName("full name");
        profileDto.setLegalOffice("legal office");
        profileDto.setTelephoneNumber("12345678");
        UpdateReferent updateReferent = new UpdateReferent();
        updateReferent.setFirstName("referent_first_name");
        updateReferent.setLastName("referent_last_name");
        updateReferent.setEmailAddress("referent.profile@pagopa.it");
        updateReferent.setTelephoneNumber("01234567");
        updateReferent.setRole("updatedRole");

        profileDto.setReferent(updateReferent);
        return profileDto;
    }

    public static DiscountEntity createSampleDiscountEntity(AgreementEntity agreement) {
        DiscountEntity discountEntity = new DiscountEntity();
        discountEntity.setState(DiscountStateEnum.DRAFT);
        discountEntity.setName("discount_name");
        discountEntity.setDescription("discount_description");
        discountEntity.setDiscountValue(15);
        discountEntity.setCondition("discount_condition");
        discountEntity.setStartDate(LocalDate.now());
        discountEntity.setEndDate(LocalDate.now().plusMonths(6));
        discountEntity.setStaticCode("discount_static_code");
        discountEntity.setAgreement(agreement);
        discountEntity.setProducts(getProductEntityList(discountEntity));
        return discountEntity;
    }

    public static List<DiscountProductEntity> getProductEntityList(DiscountEntity discountEntity) {
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

    public static List<DocumentEntity> createSampleDocumentList(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = new ArrayList<>();
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.AGREEMENT));
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.MANIFESTATION_OF_INTEREST));
        return documentList;
    }

    public static DocumentEntity createDocument(AgreementEntity agreementEntity, DocumentTypeEnum documentTypeEnum) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentType(documentTypeEnum);
        documentEntity.setDocumentUrl("file_" + documentTypeEnum.getCode());
        documentEntity.setAgreement(agreementEntity);
        return documentEntity;
    }

    public static String getJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper.writeValueAsString(obj);
    }
}
