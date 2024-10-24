package it.gov.pagopa.cgn.portal;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.resourcemanager.apimanagement.fluent.models.SubscriptionContractInner;
import com.azure.resourcemanager.apimanagement.fluent.models.SubscriptionKeysContractInner;
import com.azure.resourcemanager.apimanagement.models.SubscriptionContract;
import com.azure.resourcemanager.apimanagement.models.SubscriptionKeysContract;
import com.azure.resourcemanager.apimanagement.models.SubscriptionState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.cgn.portal.converter.discount.DiscountConverter;
import it.gov.pagopa.cgn.portal.enums.*;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import it.gov.pagopa.cgn.portal.util.CsvUtils;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import it.gov.pagopa.cgnonboardingportal.model.*;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtils {

    public static final String AGREEMENTS_CONTROLLER_PATH = "/agreements"; // needed to bypass interceptor

    private static final String AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH = AGREEMENTS_CONTROLLER_PATH + "/";

    public static final String AGREEMENT_REQUESTS_CONTROLLER_PATH = "/agreement-requests/";

    public static final String AGREEMENT_APPROVED_CONTROLLER_PATH = "/approved-agreements/";

    public static final String PUBLIC_HELP_CONTROLLER_PATH = "/help";

    public static final String GEOLOCATION_PATH = "/geolocation-token";

    public static final String FAKE_ID = "FAKE_ID";
    public static final String FAKE_ID_2 = "FAKE_ID_2";

    public static String getProfilePath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/profile";
    }

    public static String getDiscountPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/discounts";
    }

    public static String getDiscountPublishingPath(String agreementId, Long discountId) {
        return getDiscountPath(agreementId) + "/" + discountId + "/publishing";
    }

    public static String getDiscountUnpublishingPath(String agreementId, Long discountId) {
        return getDiscountPath(agreementId) + "/" + discountId + "/unpublishing";
    }

    public static String getDocumentPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/documents";
    }

    public static String getAgreementApprovalPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/approval";
    }

    public static String getUploadImagePath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/images";
    }

    public static String getUploadBucketPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/discounts/bucket";
    }

    public static String getBackofficeDocumentPath(String agreementId) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH + agreementId + "/documents";
    }

    public static String getAuthenticatedHelpPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/help";
    }

    public static String getAgreementRequestsDiscountPath(String agreementId, String discountId) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH + agreementId + "/discounts/" + discountId;
    }

    public static String getAgreementRequestsDiscountBucketCodePath(String agreementId, String discountId) {
        return getAgreementRequestsDiscountPath(agreementId, discountId) + "/bucket-code";
    }

    public static String getAgreementRequestsDiscountTestPassedPath(String agreementId, String discountId) {
        return getAgreementRequestsDiscountPath(agreementId, discountId) + "/test-passed";
    }

    public static String getAgreementRequestsDiscountTestFailedPath(String agreementId, String discountId) {
        return getAgreementRequestsDiscountPath(agreementId, discountId) + "/test-failed";
    }

    public static String getAgreementRequestsWithStatusFilterPath(String state, Optional<String> assigneeOpt) {
        StringBuilder path = new StringBuilder(AGREEMENT_REQUESTS_CONTROLLER_PATH);
        path.append("?states=").append(state);
        assigneeOpt.ifPresent(assignee -> path.append("&assignee=").append(assignee));
        return path.toString();
    }

    public static String getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum columnEnum,
                                                              Sort.Direction direction) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH +
                "?sortColumn=" +
                columnEnum.getValue() +
                "&sortDirection=" +
                direction.name();
    }

    public static String getAgreementApprovalWithSortedColumn(BackofficeApprovedSortColumnEnum columnEnum,
                                                              Sort.Direction direction) {
        return AGREEMENT_APPROVED_CONTROLLER_PATH +
                "?sortColumn=" +
                columnEnum.getValue() +
                "&sortDirection=" +
                direction.name();
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
        return createSampleProfileWithCommonFields(DiscountCodeTypeEnum.STATIC);
    }

    public static ProfileEntity createSampleProfileWithCommonFields(DiscountCodeTypeEnum discountCodeType) {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setFullName("FULL_NAME");
        profileEntity.setName("NAME");
        profileEntity.setNameEn("NAME_EN");
        profileEntity.setNameDe("NAME_DE");
        profileEntity.setTaxCodeOrVat("abcdeghilmnopqrs");
        profileEntity.setPecAddress("pec.address@pagopa.it");
        profileEntity.setDescription("A Description");
        profileEntity.setDescriptionEn("A Description EN");
        profileEntity.setDescriptionDe("A Description DE");
        profileEntity.setReferent(createSampleReferent(profileEntity));
        profileEntity.setLegalRepresentativeTaxCode("abcdeghilmnopqrs");
        profileEntity.setLegalRepresentativeFullName("full name");
        profileEntity.setLegalOffice("legal office");
        profileEntity.setDiscountCodeType(discountCodeType);
        profileEntity.setTelephoneNumber("12345678");
        profileEntity.setAllNationalAddresses(true);
        profileEntity.setSupportType(SupportTypeEnum.PHONENUMBER);
        profileEntity.setSupportValue("0000000");
        return profileEntity;
    }

    public static UpdateProfile updatableOnlineProfileFromProfileEntity(ProfileEntity profileEntity,
                                                                        DiscountCodeType discountCodeType) {
        OnlineChannel salesChannel = new OnlineChannel();
        salesChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
        salesChannel.setWebsiteUrl("anurl.com");
        salesChannel.setDiscountCodeType(discountCodeType);
        return updatableProfileFromProfileEntity(profileEntity, salesChannel);
    }

    public static UpdateProfile updatableOfflineProfileFromProfileEntity(ProfileEntity profileEntity) {
        Address address = new Address();
        address.setFullAddress("Via unavia, n.1, 30000, Veneto");

        OfflineChannel salesChannel = new OfflineChannel();
        salesChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
        salesChannel.setWebsiteUrl("anurl.com");
        salesChannel.setAddresses(Stream.of(address).collect(Collectors.toList()));
        salesChannel.setAllNationalAddresses(true);
        return updatableProfileFromProfileEntity(profileEntity, salesChannel);
    }

    public static UpdateProfile updatableProfileFromProfileEntity(ProfileEntity profileEntity,
                                                                  SalesChannel salesChannel) {
        UpdateReferent referent = new UpdateReferent();
        referent.setEmailAddress(profileEntity.getReferent().getEmailAddress());
        referent.setFirstName(profileEntity.getReferent().getFirstName());
        referent.setTelephoneNumber(profileEntity.getReferent().getTelephoneNumber());
        referent.setLastName(profileEntity.getReferent().getLastName());
        referent.setRole(profileEntity.getReferent().getRole());

        UpdateProfile updateProfile = new UpdateProfile();
        updateProfile.setDescription(profileEntity.getDescription());
        updateProfile.setDescriptionEn(profileEntity.getDescriptionEn());
        updateProfile.setDescriptionDe(profileEntity.getDescriptionDe());
        updateProfile.setSalesChannel(salesChannel);
        updateProfile.setName(profileEntity.getName());
        updateProfile.setNameEn(profileEntity.getNameEn());
        updateProfile.setNameDe(profileEntity.getNameDe());
        updateProfile.setLegalOffice(profileEntity.getLegalOffice());
        updateProfile.setReferent(referent);
        updateProfile.setPecAddress(profileEntity.getPecAddress());
        updateProfile.setTelephoneNumber(profileEntity.getTelephoneNumber());
        updateProfile.setLegalRepresentativeFullName(profileEntity.getLegalRepresentativeFullName());
        updateProfile.setLegalRepresentativeTaxCode(profileEntity.getLegalRepresentativeTaxCode());
        updateProfile.setSecondaryReferents(createUpdateReferentList());

        return updateProfile;
    }

    public static CreateProfile offLineProfileFromProfileEntity(ProfileEntity profileEntity) {
        Address address = new Address();
        address.setFullAddress("Via unavia, n.1, 30000, Veneto");

        OfflineChannel salesChannel = new OfflineChannel();
        salesChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
        salesChannel.setWebsiteUrl("anurl.com");
        salesChannel.setAddresses(Stream.of(address).collect(Collectors.toList()));
        salesChannel.setAllNationalAddresses(true);
        return createProfileFromProfileEntity(profileEntity, salesChannel);
    }


    public static CreateProfile createProfileFromProfileEntity(ProfileEntity profileEntity,
                                                               SalesChannel salesChannel) {
        CreateReferent referent = new CreateReferent();
        referent.setEmailAddress(profileEntity.getReferent().getEmailAddress());
        referent.setFirstName(profileEntity.getReferent().getFirstName());
        referent.setTelephoneNumber(profileEntity.getReferent().getTelephoneNumber());
        referent.setLastName(profileEntity.getReferent().getLastName());
        referent.setRole(profileEntity.getReferent().getRole());

        CreateProfile createProfile = new CreateProfile();
        createProfile.setFullName("Full name");
        createProfile.setTaxCodeOrVat("BGCMNN80V12K909Z");
        createProfile.setDescription(profileEntity.getDescription());
        createProfile.setDescriptionEn(profileEntity.getDescriptionEn());
        createProfile.setDescriptionDe(profileEntity.getDescriptionDe());
        createProfile.setSalesChannel(salesChannel);
        createProfile.setName(profileEntity.getName());
        createProfile.setNameEn(profileEntity.getNameEn());
        createProfile.setNameDe(profileEntity.getNameDe());
        createProfile.setLegalOffice(profileEntity.getLegalOffice());
        createProfile.setReferent(referent);
        createProfile.setPecAddress(profileEntity.getPecAddress());
        createProfile.setTelephoneNumber(profileEntity.getTelephoneNumber());
        createProfile.setLegalRepresentativeFullName(profileEntity.getLegalRepresentativeFullName());
        createProfile.setLegalRepresentativeTaxCode(profileEntity.getLegalRepresentativeTaxCode());
        createProfile.setSecondaryReferents(createCreateReferentList());

        return createProfile;
    }


    public static List<AddressEntity> createSampleAddress(ProfileEntity profileEntity) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setProfile(profileEntity);
        addressEntity.setFullAddress("GARIBALDI 1 00100 Rome RM");
        addressEntity.setLatitude(42.92439);
        addressEntity.setLongitude(12.50181);
        List<AddressEntity> list = new ArrayList<>(1);
        list.add(addressEntity);
        return list;
    }

    public static List<Address> createSampleAddressDto() {
        Address address = new Address();
        address.setFullAddress("GARIBALDI 1 00100 Rome RM");
        Coordinates coordinates = new Coordinates();
        coordinates.setLongitude(BigDecimal.valueOf(9.1890953));
        coordinates.setLatitude(BigDecimal.valueOf(45.489751));
        address.setCoordinates(coordinates);
        return Collections.singletonList(address);
    }

    public static AgreementEntity createSampleAgreementEntityWithCommonFields() {
        AgreementEntity agreementEntity = new AgreementEntity();
        agreementEntity.setId("agreement_id");
        agreementEntity.setImageUrl("image12345.png");
        return agreementEntity;
    }

    public static ProfileEntity createSampleProfileEntity(AgreementEntity agreementEntity) {
        return createSampleProfileEntity(agreementEntity, SalesChannelEnum.ONLINE, DiscountCodeTypeEnum.STATIC);
    }

    public static ProfileEntity createSampleProfileEntity(AgreementEntity agreementEntity,
                                                          SalesChannelEnum salesChannel,
                                                          DiscountCodeTypeEnum discountCodeType) {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields(discountCodeType);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(salesChannel);
        profileEntity.setAgreement(agreementEntity);
        return profileEntity;
    }

    public static ProfileEntity createProfileEntityWithSecondaryEntityReferentList(AgreementEntity agreementEntity) {
        ProfileEntity profileEntity = createSampleProfileEntity(agreementEntity);
        profileEntity.setSecondaryReferentList(createSampleSecondaryReferentEntityList(profileEntity));
        return profileEntity;
    }

    private static List<SecondaryReferentEntity> createSampleSecondaryReferentEntityList(ProfileEntity profileEntity) {

        SecondaryReferentEntity secondaryReferentEntity_0 = new SecondaryReferentEntity();
        secondaryReferentEntity_0.setFirstName("FIRST_NAME_0");
        secondaryReferentEntity_0.setLastName("LAST_NAME_0");
        secondaryReferentEntity_0.setEmailAddress("referent.registry_0@pagopa.it");
        secondaryReferentEntity_0.setTelephoneNumber("+390123456789");
        secondaryReferentEntity_0.setProfile(profileEntity);
        secondaryReferentEntity_0.setRole("CEO");

        SecondaryReferentEntity secondaryReferentEntity_1 = new SecondaryReferentEntity();
        secondaryReferentEntity_1.setFirstName("FIRST_NAME_1");
        secondaryReferentEntity_1.setLastName("LAST_NAME_1");
        secondaryReferentEntity_1.setEmailAddress("referent.registry_1@pagopa.it");
        secondaryReferentEntity_1.setTelephoneNumber("+390123456789");
        secondaryReferentEntity_1.setProfile(profileEntity);
        secondaryReferentEntity_1.setRole("CEO");

        return new ArrayList<>(Arrays.asList(secondaryReferentEntity_0, secondaryReferentEntity_1));
    }

    public static UpdateProfile createSampleUpdateProfileWithCommonFields() {
        UpdateProfile profileDto = new UpdateProfile();
        profileDto.setName("name_dto");
        profileDto.setNameEn("name_dto_en");
        profileDto.setNameDe("name_dto_de");
        profileDto.setDescription("description_dto");
        profileDto.setDescriptionEn("description_dto_en");
        profileDto.setDescriptionDe("description_dto_de");
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

    public static it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest createSamplePublicApiHelpRequest() {
        it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest helpRequest
                = new it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest();
        helpRequest.setCategory(it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest.CategoryEnum.ACCESS);
        helpRequest.setTopic("a topic");
        helpRequest.setMessage("I need help");
        helpRequest.setEmailAddress("myname.help@pagopa.it");
        helpRequest.setLegalName("PagoPa");
        helpRequest.setReferentFirstName("Me");
        helpRequest.setReferentLastName("You");
        helpRequest.setRecaptchaToken("token");
        return helpRequest;
    }

    public static it.gov.pagopa.cgnonboardingportal.model.HelpRequest createSampleAuthenticatedHelpRequest() {
        it.gov.pagopa.cgnonboardingportal.model.HelpRequest helpRequest
                = new it.gov.pagopa.cgnonboardingportal.model.HelpRequest();
        helpRequest.setCategory(it.gov.pagopa.cgnonboardingportal.model.HelpRequest.CategoryEnum.ACCESS);
        helpRequest.setTopic("a topic");
        helpRequest.setMessage("I need help");
        return helpRequest;
    }

    public static DiscountEntity createSampleDiscountEntityWithStaticCode(AgreementEntity agreement,
                                                                          String staticCode) {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreement);
        discountEntity.setStaticCode(staticCode);
        discountEntity.setLandingPageUrl(null);
        discountEntity.setLandingPageReferrer(null);
        discountEntity.setDiscountUrl("https://anurl.com");
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithLandingPage(AgreementEntity agreement,
                                                                           String url,
                                                                           String referrer) {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreement);
        discountEntity.setStaticCode(null);
        discountEntity.setLandingPageUrl(url);
        discountEntity.setLandingPageReferrer(referrer);
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithBucketCodes(AgreementEntity agreement) {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreement);
        discountEntity.setStaticCode(null);
        discountEntity.setLastBucketCodeLoadUid(generateDiscountBucketCodeUid());
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        return discountEntity;
    }

    public static BucketCodeLoadEntity createDummyBucketLoadEntity(Long discountId) {
        BucketCodeLoadEntity bucketCodeLoadEntity = new BucketCodeLoadEntity();
        bucketCodeLoadEntity.setId(1L);
        bucketCodeLoadEntity.setUid(generateDiscountBucketCodeUid());
        bucketCodeLoadEntity.setFileName("codes.txt");
        bucketCodeLoadEntity.setDiscountId(discountId);
        bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.PENDING);
        bucketCodeLoadEntity.setNumberOfCodes(100L);
        return bucketCodeLoadEntity;
    }

    public static DiscountEntity createSampleDiscountEntity(AgreementEntity agreement) {
        DiscountEntity discountEntity = createSampleDiscountEntityWithoutProduct(agreement);
        discountEntity.setProducts(getProductEntityList(discountEntity));
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithoutProduct(AgreementEntity agreement) {
        DiscountEntity discountEntity = new DiscountEntity();
        discountEntity.setState(DiscountStateEnum.DRAFT);
        discountEntity.setName("discount_name");
        discountEntity.setNameEn("discount_name_en");
        discountEntity.setNameDe("discount_name_de");
        discountEntity.setDescription("discount_description");
        discountEntity.setDescriptionEn("discount_description_en");
        discountEntity.setDescriptionDe("discount_description_de");
        discountEntity.setDiscountValue(15);
        discountEntity.setCondition("discount_condition");
        discountEntity.setConditionEn("discount_condition_en");
        discountEntity.setConditionDe("discount_condition_de");
        discountEntity.setStartDate(LocalDate.now());
        discountEntity.setEndDate(LocalDate.now().plusMonths(6));
        discountEntity.setAgreement(agreement);
        discountEntity.setStaticCode("static_code");
        discountEntity.setVisibleOnEyca(false);
        discountEntity.setDiscountUrl("anurl.com");
        return discountEntity;
    }

    public static List<DiscountProductEntity> getProductEntityList(DiscountEntity discountEntity) {
        List<DiscountProductEntity> productEntityList = new ArrayList<>();
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.TRAVELLING);
        productEntityList.add(productEntity);
        productEntityList.forEach(p -> p.setDiscount(discountEntity));
        return productEntityList;
    }

    public static List<EycaDataExportViewEntity> getListWIthLandingPageAndReferent() {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setCategories("products");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setDiscountType("LANDING PAGE");
        entity_0.setReferent(1L);
        entity_0.setLive("Y");
        entity_0.setDiscountId(7L);

        return Collections.singletonList(entity_0);

    }


    private static EycaDataExportViewEntity getRealEycaDataExportViewEntity_0(Long discountId, String eycaUpdateId) {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(40L);
        entity_0.setCategories("SV");
        entity_0.setProfileId(134L);
        entity_0.setDiscountId(discountId);
        entity_0.setReferent(134L);
        entity_0.setEycaUpdateId(eycaUpdateId);
        entity_0.setVendor("Dipartimento per le Politiche giovanili e il Servizio civile universale");
        entity_0.setName("-");
        entity_0.setNameLocal("OpportunitÃ  di Stage con Milano Premier Padel P1");
        entity_0.setText("- - - - To access the discount, show your EYCA card at the point of sale.");
        entity_0.setTextLocal("Durante l'evento,allo stand del Ministro per lo Sport e i Giovani,Ã¨ possibile presentare il proprio CV per uno stage di 6 mesi nell'organizzazione di eventi sportivi internazionali.E' previsto un rimborso spese mensile di â‚¬400 per 20 ore a settimana. - Necessaria l'iscrizione a un corso di laurea specialistica,master o dottorato,oltre a un eccellente livello di inglese. Requisiti e ulteriori informazioni al link. - Per accedere all'agevolazione, mostra la tua carta EYCA presso il punto vendita.");
        entity_0.setStartDate(LocalDate.of(2023, 11, 29));
        entity_0.setEndDate(LocalDate.of(2023, 12, 10));
        entity_0.setEmail("");
        entity_0.setPhone("");
        entity_0.setWeb(null);
        entity_0.setTags("");
        entity_0.setImage("https://cgnonboardingportal-p-cdnendpoint-storage.azureedge.net/profileimages/image-2eb38226-928f-40e7-8a06-eecb4a15cb1f.jpg");
        entity_0.setLocationLocalId("");
        entity_0.setStreet(null);
        entity_0.setCity("");
        entity_0.setZip("");
        entity_0.setCountry("");
        entity_0.setRegion("");
        entity_0.setLongitude("");
        entity_0.setLatitude("");
        entity_0.setDiscountType("SHOP");
        entity_0.setLive("Y");

        return entity_0;
    }


    private static EycaDataExportViewEntity getRealEycaDataExportViewEntity_1(Long discountId, String eycaUpdateId) {
        EycaDataExportViewEntity entity_1 = new EycaDataExportViewEntity();
        entity_1.setId(26L);
        entity_1.setCategories("LR,SV");
        entity_1.setProfileId(134L);
        entity_1.setDiscountId(discountId);
        entity_1.setReferent(134L);
        entity_1.setEycaUpdateId(eycaUpdateId);
        entity_1.setVendor("Dipartimento per le Politiche giovanili e il Servizio civile universale");
        entity_1.setName("Giovani2030");
        entity_1.setNameLocal("Giovani2030");
        entity_1.setText("GIOVANI2030 is the digital home created by Dipartimento per le politiche giovanili e il Servizio civile universale for those willing to get new tools, face new challenges and find their own way. - If you are between 14 and 35 years old, you are an italian resident and you want to get opportunities for education, volunteering, national and foreign calls, GIOVANI2030 is the right place for you! - To access the discount, show your EYCA card at the point of sale.");
        entity_1.setTextLocal("GIOVANI2030 Ã¨ la casa digitale creata dal Dipartimento per le politiche giovanili e il Servizio civile universale proprio per chi, come te, cerca nuovi strumenti e nuove sfide per crescere e trovare la propria strada. - Se hai tra i 14 e i 35 anni, risiedi in Italia e vuoi conoscere le migliori opportunitÃ  di formazione, volontariato, cultura e bandi nazionali ed esteri, GIOVANI2030 Ã¨ il posto giusto per te! - Per accedere all'agevolazione, mostra la tua carta EYCA presso il punto vendita.");
        entity_1.setStartDate(LocalDate.of(2023, 4, 17));
        entity_1.setEndDate(LocalDate.of(2023, 12, 31));
        entity_1.setEmail("");
        entity_1.setPhone("");
        entity_1.setWeb(null);
        entity_1.setTags("");
        entity_1.setImage("https://cgnonboardingportal-p-cdnendpoint-storage.azureedge.net/profileimages/image-2eb38226-928f-40e7-8a06-eecb4a15cb1f.jpg");
        entity_1.setLocationLocalId("");
        entity_1.setStreet(null);
        entity_1.setCity("");
        entity_1.setZip("");
        entity_1.setCountry("");
        entity_1.setRegion("");
        entity_1.setLongitude("");
        entity_1.setLatitude("");
        entity_1.setDiscountType("SHOP");
        entity_1.setLive("Y");

        return entity_1;
    }


    public static List<EycaDataExportViewEntity> getRealDataList() {
        return Arrays.asList(getRealEycaDataExportViewEntity_0(500L, null), getRealEycaDataExportViewEntity_1(501L, null),
                getRealEycaDataExportViewEntity_0(502L, "c49020231110173105078447"), getRealEycaDataExportViewEntity_1(503L, "c49020232220173105078447"));
    }
    public static List<EycaDataExportViewEntity> getRealDataListForSync() {
        return Arrays.asList(getRealEycaDataExportViewEntity_0(502L, "c49020231110173105078447"));
    }
    public static List<EycaDataExportViewEntity> getEycaDataExportViewEntityListFromCSV() {
    	return CsvUtils.csvToEntityList(TestUtils.class.getClassLoader().getResourceAsStream("eyca_data_export.csv"), 
    		(_record) -> {
    			EycaDataExportViewEntity e = new EycaDataExportViewEntity();
    			
    			e.setId(Long.valueOf(_record.get("id")));
    			e.setCategories(_record.get("categories"));
    			e.setProfileId(Long.valueOf(_record.get("profile_id")));
    			e.setVendor(_record.get("vendor"));
    			e.setDiscountId(Long.valueOf(_record.get("discount_id")));
    			e.setEycaUpdateId(_record.get("eyca_update_id"));
    			e.setName(_record.get("name"));
    			e.setStartDate(LocalDate.parse(_record.get("start_date")));
    			e.setEndDate(LocalDate.parse(_record.get("end_date")));
    			e.setNameLocal(_record.get("name_local"));
    			e.setText(_record.get("text"));
    			e.setTextLocal(_record.get("text_local"));
    			e.setEmail(_record.get("email"));
    			e.setPhone(_record.get("phone"));
    			e.setWeb(_record.get("web"));
    			e.setTags(_record.get("tags"));
    			e.setImage(_record.get("image"));
    			e.setLive(_record.get("live"));
    			e.setLocationLocalId(_record.get("location_local_id"));
    			e.setStreet(_record.get("street"));
    			e.setCity(_record.get("city"));
    			e.setZip(_record.get("zip"));
    			e.setCountry(_record.get("country"));
    			e.setRegion(_record.get("region"));
    			e.setLatitude(_record.get("latitude"));
    			e.setLongitude(_record.get("longitude"));
    			e.setDiscountType(_record.get("discount_type"));
    			e.setReferent(Long.valueOf(_record.get("referent")));
    			
    		return e;
    	});
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportForCreate() {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setCategories("products");
        entity_0.setEycaUpdateId("");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setDiscountType("LANDING PAGE");
        entity_0.setLive("Y");
        entity_0.setDiscountId(1L);
       return List.of(entity_0);
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportForUpdate() {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setCategories("products");
        entity_0.setEycaUpdateId("1234");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setDiscountType("LANDING PAGE");
        entity_0.setLive("Y");
        entity_0.setDiscountId(1L);
        return List.of(entity_0);
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportViewEntityList() {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setCategories("products");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setDiscountType("LANDING PAGE");
        entity_0.setLive("Y");
        entity_0.setDiscountId(1L);

        EycaDataExportViewEntity entity_1 = new EycaDataExportViewEntity();
        entity_1.setId(1L);
        entity_1.setCategories("products");
        entity_1.setProfileId(1L);
        entity_1.setVendor("vendor_0");
        entity_1.setName("name_0");
        entity_1.setNameLocal("name_local_0");
        entity_1.setDiscountType("LANDING PAGE");
        entity_1.setLive("Y");
        entity_1.setDiscountId(2L);
        entity_1.setCountry("italy");
        entity_1.setCity("city");
        entity_1.setStreet("address0");
        entity_1.setLatitude("48");
        entity_1.setLongitude("12");

        EycaDataExportViewEntity entity_2 = new EycaDataExportViewEntity();
        entity_2.setId(1L);
        entity_2.setCategories("products");
        entity_2.setProfileId(1L);
        entity_2.setVendor("vendor_0");
        entity_2.setName("name_0");
        entity_2.setNameLocal("name_local_0");
        entity_2.setDiscountType("LANDING PAGE");
        entity_2.setLive("Y");
        entity_2.setDiscountId(3L);
        entity_2.setCity("city");
        entity_2.setStreet("address0");
        entity_2.setLatitude("48");
        entity_2.setLongitude("12");

        EycaDataExportViewEntity entity_3 = new EycaDataExportViewEntity();
        entity_3.setId(1L);
        entity_3.setCategories("products");
        entity_3.setProfileId(1L);
        entity_3.setVendor("vendor_0");
        entity_3.setName("name_0");
        entity_3.setNameLocal("name_local_0");
        entity_3.setDiscountType("LANDING PAGE");
        entity_3.setLive("Y");
        entity_3.setDiscountId(4L);
        entity_3.setCountry("italy");
        entity_3.setStreet("address0");
        entity_3.setLatitude("48");
        entity_3.setLongitude("12");

        EycaDataExportViewEntity entity_4 = new EycaDataExportViewEntity();
        entity_4.setId(1L);
        entity_4.setCategories("products");
        entity_4.setProfileId(1L);
        entity_4.setVendor("vendor_0");
        entity_4.setName("name_0");
        entity_4.setNameLocal("name_local_0");
        entity_4.setDiscountType("LANDING PAGE");
        entity_4.setLive("Y");
        entity_4.setDiscountId(5L);
        entity_4.setCountry("italy");
        entity_4.setCity("city");
        entity_4.setLatitude("48");
        entity_4.setLongitude("12");

        EycaDataExportViewEntity entity_5 = new EycaDataExportViewEntity();
        entity_5.setId(1L);
        entity_5.setCategories("products");
        entity_5.setProfileId(1L);
        entity_5.setVendor("vendor_0");
        entity_5.setName("name_0");
        entity_5.setNameLocal("name_local_0");
        entity_5.setDiscountType("LANDING PAGE");
        entity_5.setLive("Y");
        entity_5.setDiscountId(6L);
        entity_5.setCountry("italy");
        entity_5.setCity("city");
        entity_5.setStreet("address0");
        entity_5.setLongitude("12");
        entity_5.setTags("tag1, tag2");

        EycaDataExportViewEntity entity_6 = new EycaDataExportViewEntity();
        entity_6.setId(1L);
        entity_6.setCategories("products");
        entity_6.setProfileId(1L);
        entity_6.setVendor("vendor_0");
        entity_6.setName("name_0");
        entity_6.setNameLocal("name_local_0");
        entity_6.setStreet("address0");
        entity_6.setDiscountType("mode0");
        entity_6.setDiscountId(7L);
        entity_6.setLive(null);
        entity_6.setEndDate(LocalDate.now().minusDays(4));

        EycaDataExportViewEntity entity_7 = new EycaDataExportViewEntity();
        entity_7.setId(2L);
        entity_7.setCategories("products");
        entity_7.setProfileId(1L);
        entity_7.setVendor("vendor_0");
        entity_7.setName("name_0");
        entity_7.setNameLocal("name_local_0");
        entity_7.setStreet("address0");
        entity_7.setDiscountType("mode0");
        entity_7.setLive("N");
        entity_7.setDiscountId(8L);
        entity_7.setEndDate(LocalDate.now().minusDays(2));

        EycaDataExportViewEntity entity_8 = new EycaDataExportViewEntity();
        entity_8.setId(1L);
        entity_8.setCategories("products");
        entity_8.setProfileId(1L);
        entity_8.setVendor("vendor_0");
        entity_8.setName("name_0");
        entity_8.setNameLocal("name_local_0");
        entity_8.setStreet("address0");
        entity_8.setDiscountType("LANDING PAGE");
        entity_8.setLive("y");
        entity_8.setDiscountId(9L);
        entity_8.setEycaUpdateId("655464565");

        EycaDataExportViewEntity entity_9 = new EycaDataExportViewEntity();
        entity_9.setId(1L);
        entity_9.setCategories("products");
        entity_9.setProfileId(1L);
        entity_9.setVendor("vendor_1");
        entity_9.setName("name_1");
        entity_9.setNameLocal("name_local_1");
        entity_9.setStreet("address1");
        entity_9.setDiscountType("LANDING PAGE");
        entity_9.setLive("N");
        entity_9.setDiscountId(10L);
        entity_9.setEycaUpdateId("650054665");
        entity_9.setEndDate(LocalDate.now().minusDays(4));

        EycaDataExportViewEntity entity_10 = new EycaDataExportViewEntity();

        entity_10.setId(1L);
        entity_10.setCategories("products");
        entity_10.setProfileId(1L);
        entity_10.setVendor("vendor_2");
        entity_10.setName("name_2");
        entity_10.setNameLocal("name_local_2");
        entity_10.setStreet("address2");
        entity_10.setDiscountType("LANDING PAGE");
        entity_10.setLive("N");
        entity_10.setDiscountId(10L);
        entity_10.setEycaUpdateId("6551114565");
        entity_10.setEndDate(LocalDate.now());

        EycaDataExportViewEntity entity_11 = new EycaDataExportViewEntity();

        entity_11.setId(1L);
        entity_11.setCategories("products");
        entity_11.setProfileId(1L);
        entity_11.setVendor("vendor_2");
        entity_11.setName("name_2");
        entity_11.setNameLocal("name_local_2");
        entity_11.setStreet("address2");
        entity_11.setDiscountType("LANDING PAGE");
        entity_11.setLive("Y");
        entity_11.setDiscountId(11L);
        entity_11.setEycaUpdateId("6551114565");
        entity_11.setEndDate(LocalDate.now().plusDays(2));

        EycaDataExportViewEntity entity_12 = new EycaDataExportViewEntity();
        entity_12.setId(1L);
        entity_12.setCategories("products");
        entity_12.setProfileId(1L);
        entity_12.setVendor("vendor_0");
        entity_12.setName("name_0");
        entity_12.setNameLocal("name_local_0");
        entity_12.setStreet("address0");
        entity_12.setLive("Y");
        entity_12.setDiscountId(12L);
        entity_12.setEycaUpdateId("655464565");

        EycaDataExportViewEntity entity_13 = new EycaDataExportViewEntity();
        entity_13.setId(1L);
        entity_13.setCategories("products");
        entity_13.setProfileId(1L);
        entity_13.setVendor("vendor_0");
        entity_13.setName("name_0");
        entity_13.setNameLocal("name_local_0");
        entity_13.setStreet("address0");
        entity_13.setLive("Y");
        entity_13.setDiscountId(13L);
        entity_13.setDiscountType("LIST OF STATIC CODES");

        EycaDataExportViewEntity entity_14 = new EycaDataExportViewEntity();
        entity_14.setId(1L);
        entity_14.setCategories("products");
        entity_14.setProfileId(1L);
        entity_14.setVendor("vendor_0");
        entity_14.setName("name_0");
        entity_14.setNameLocal("name_local_0");
        entity_14.setStreet("address0");
        entity_14.setDiscountType("mode0");
        entity_14.setLive("Y");
        entity_14.setDiscountId(14L);

        EycaDataExportViewEntity entity_15 = new EycaDataExportViewEntity();
        entity_15.setId(1L);
        entity_15.setCategories("products");
        entity_15.setProfileId(1L);
        entity_15.setVendor("vendor_0");
        entity_15.setName("name_0");
        entity_15.setNameLocal("name_local_0");
        entity_15.setDiscountType("LIST OF STATIC CODES");
        entity_15.setLive("Y");
        entity_15.setDiscountId(15L);

        EycaDataExportViewEntity entity_16 = new EycaDataExportViewEntity();
        entity_16.setId(1L);
        entity_16.setCategories("products");
        entity_16.setEycaUpdateId("hughgt7y98565");
        entity_16.setProfileId(1L);
        entity_16.setVendor("vendor_0");
        entity_16.setName("name_0");
        entity_16.setNameLocal("name_local_0");
        entity_16.setDiscountType("LIST OF STATIC CODES");
        entity_16.setLive("Y");
        entity_16.setDiscountId(16L);


        return Arrays.asList(entity_0, entity_1, entity_2, entity_3, entity_4, entity_5, entity_6,
                entity_7, entity_8, entity_9, entity_10, entity_11, entity_12, entity_13, entity_14, entity_15, entity_16);
    }


    public static List<EycaDataExportViewEntity> getTobeDeletedEycaDataExportViewEntityList() {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setCategories("products");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setDiscountType("LANDING PAGE");
        entity_0.setLive("N");
        entity_0.setDiscountId(6L);
        entity_0.setEycaUpdateId("ce00958658596");
        entity_0.setEndDate(LocalDate.now());

        EycaDataExportViewEntity entity_1 = new EycaDataExportViewEntity();
        entity_1.setId(2L);
        entity_1.setCategories("products");
        entity_1.setProfileId(1L);
        entity_1.setVendor("vendor_0");
        entity_1.setName("name_0");
        entity_1.setNameLocal("name_local_0");
        entity_1.setDiscountType("LANDING PAGE");
        entity_1.setLive(null);
        entity_1.setEndDate(LocalDate.now());
        entity_1.setDiscountId(7L);
        entity_1.setEycaUpdateId("ce00957778596");

        EycaDataExportViewEntity entity_2 = new EycaDataExportViewEntity();
        entity_2.setId(1L);
        entity_2.setCategories("products");
        entity_2.setProfileId(1L);
        entity_2.setVendor("vendor_0");
        entity_2.setName("name_0");
        entity_2.setNameLocal("name_local_0");
        entity_2.setDiscountType("LANDING PAGE");
        entity_2.setLive("N");
        entity_2.setEndDate(LocalDate.now().minusDays(2));
        entity_2.setDiscountId(8L);
        entity_2.setEycaUpdateId("ce00958999596");

        EycaDataExportViewEntity entity_3 = new EycaDataExportViewEntity();
        entity_3.setId(1L);
        entity_3.setCategories("products");
        entity_3.setProfileId(1L);
        entity_3.setVendor("vendor_0");
        entity_3.setName("name_0");
        entity_3.setNameLocal("name_local_0");
        entity_3.setDiscountType("LANDING PAGE");
        entity_3.setLive("N");
        entity_3.setEndDate(LocalDate.now().minusDays(2));
        entity_3.setDiscountId(8L);

        return Arrays.asList(entity_0, entity_1, entity_2, entity_3);

    }

    public static List<EycaDataExportViewEntity> getListWIthNoDiscountype() {
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setCategories("products");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setLive("Y");
        entity_0.setDiscountId(7L);
        entity_0.setEycaUpdateId("655464565");

        return Collections.singletonList(entity_0);
    }


    public static String generateDiscountBucketCodeUid() {
        return UUID.randomUUID().toString();
    }

    public static UpdateDiscount updatableDiscountFromDiscountEntity(DiscountEntity discountEntity) {
        DiscountConverter discountConverter = new DiscountConverter();
        Discount discount = discountConverter.toDto(discountEntity);
        UpdateDiscount updateDiscount = new UpdateDiscount();
        updateDiscount.setName(discount.getName());
        updateDiscount.setNameEn(discount.getNameEn());
        updateDiscount.setNameDe(discount.getNameDe());
        updateDiscount.setDescription(discount.getDescription());
        updateDiscount.setDescriptionEn(discount.getDescriptionEn());
        updateDiscount.setDescriptionDe(discount.getDescriptionDe());
        updateDiscount.setCondition(discount.getCondition());
        updateDiscount.setConditionEn(discount.getConditionEn());
        updateDiscount.setConditionDe(discount.getConditionDe());
        updateDiscount.setStartDate(discount.getStartDate());
        updateDiscount.setEndDate(discount.getEndDate());
        updateDiscount.setStaticCode(discount.getStaticCode());
        updateDiscount.setLandingPageUrl(discount.getLandingPageUrl());
        updateDiscount.setLandingPageReferrer(discount.getLandingPageReferrer());
        updateDiscount.setProductCategories(discount.getProductCategories());
        updateDiscount.setLastBucketCodeLoadUid(discount.getLastBucketCodeLoadUid());
        updateDiscount.setLastBucketCodeLoadFileName(discount.getLastBucketCodeLoadFileName());
        return updateDiscount;
    }

    /*
    response.getApiResponse() != null &&
                    response.getApiResponse().getData() != null &&
                    response.getApiResponse().getData().getDiscounts() != null &&
                        ObjectUtils.isEmpty(response.getApiResponse().getData().getDiscounts().getData())
     */
    public static SearchApiResponseEyca getSearchApiResponse() {
        SearchDataExportEyca searchDataExportEyca = new SearchDataExportEyca();

        SearchApiResponseEyca searchApiResponseEyca = new SearchApiResponseEyca();
        SearchApiResponseApiResponseEyca searchApiResponseApiResponseEyca = new SearchApiResponseApiResponseEyca();
        SearchApiResponseApiResponseDataEyca searchApiResponseApiResponseDataEyca = new SearchApiResponseApiResponseDataEyca();
        SearchApiResponseApiResponseDataDiscountsEyca  searchApiResponseApiResponseDataDiscountsEyca = new  SearchApiResponseApiResponseDataDiscountsEyca();

        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");
        items.add(discountItemEyca);


        searchApiResponseApiResponseEyca.setData(searchApiResponseApiResponseDataEyca);
        searchApiResponseApiResponseDataEyca.setDiscounts(searchApiResponseApiResponseDataDiscountsEyca);
        searchApiResponseApiResponseDataDiscountsEyca.setData(items);
        searchApiResponseEyca.setApiResponse(searchApiResponseApiResponseEyca);
        return searchApiResponseEyca;
    }

    public static SearchApiResponseEyca getSearchApiResponseWithDataEmptyList() {
        SearchDataExportEyca searchDataExportEyca = new SearchDataExportEyca();

        SearchApiResponseEyca searchApiResponseEyca = new SearchApiResponseEyca();
        SearchApiResponseApiResponseEyca searchApiResponseApiResponseEyca = new SearchApiResponseApiResponseEyca();
        SearchApiResponseApiResponseDataEyca searchApiResponseApiResponseDataEyca = new SearchApiResponseApiResponseDataEyca();
        SearchApiResponseApiResponseDataDiscountsEyca  searchApiResponseApiResponseDataDiscountsEyca = new  SearchApiResponseApiResponseDataDiscountsEyca();

        searchApiResponseApiResponseEyca.setData(searchApiResponseApiResponseDataEyca);
        searchApiResponseApiResponseDataEyca.setDiscounts(searchApiResponseApiResponseDataDiscountsEyca);
        searchApiResponseApiResponseDataDiscountsEyca.setData(Collections.emptyList());
        searchApiResponseEyca.setApiResponse(searchApiResponseApiResponseEyca);
        return searchApiResponseEyca;
    }

    public static ApiResponseEyca getApiResponse() {
        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);
        apiResponseDataEyca.setDiscount(items);
        apiResponseApiResponseEyca.setData(apiResponseDataEyca);
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        return apiResponseEyca;
    }

    public static ApiResponseEyca getIncompleteApiResponse_0() {
        return new ApiResponseEyca();
    }

    public static ApiResponseEyca getIncompleteApiResponse_1() {
        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        return apiResponseEyca;
    }

    public static ApiResponseEyca getIncompleteApiResponse_2() {
        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        apiResponseApiResponseEyca.setData(apiResponseDataEyca);
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        return apiResponseEyca;
    }


    public static DeleteApiResponseEyca getDeleteApiResponse() {
        DeleteApiResponseEyca apiResponseEyca = new DeleteApiResponseEyca();

        DeleteApiResponseApiResponseEyca deleteApiResponseApiResponseEyca = new DeleteApiResponseApiResponseEyca();
        deleteApiResponseApiResponseEyca.setError(0);
        deleteApiResponseApiResponseEyca.setCode(1);
        deleteApiResponseApiResponseEyca.setText("DELETED");
        deleteApiResponseApiResponseEyca.setData(null);
        apiResponseEyca.setApiResponse(deleteApiResponseApiResponseEyca);

        return apiResponseEyca;
    }

    public static List<DocumentEntity> createSamplePaDocumentList(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = new ArrayList<>();
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.AGREEMENT));
        return documentList;
    }

    public static List<DocumentEntity> createSampleDocumentList(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = new ArrayList<>();
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.AGREEMENT));
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.ADHESION_REQUEST));
        return documentList;
    }

    public static List<DocumentEntity> createSampleBackofficeDocumentList(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = new ArrayList<>();
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.BACKOFFICE_AGREEMENT));
        return documentList;
    }

    public static DocumentEntity createDocument(AgreementEntity agreementEntity, DocumentTypeEnum documentTypeEnum) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentType(documentTypeEnum);
        documentEntity.setDocumentUrl("file_" + documentTypeEnum.getCode() + agreementEntity.getId());
        documentEntity.setAgreement(agreementEntity);
        return documentEntity;
    }

    public static List<UpdateReferent> createUpdateReferentList() {
        UpdateReferent updateReferent_0 = new UpdateReferent();
        updateReferent_0.setEmailAddress("mail_a.mail@mail.com");
        updateReferent_0.setFirstName("FIRSTNAME_A");
        updateReferent_0.setLastName("LASTNAME_A");
        updateReferent_0.setTelephoneNumber("54654654");
        updateReferent_0.setRole("ROLE");

        UpdateReferent updateReferent_1 = new UpdateReferent();
        updateReferent_1.setEmailAddress("mail_b.mail@mail.com");
        updateReferent_1.setFirstName("FIRSTNAME_B");
        updateReferent_1.setLastName("LASTNAME_B");
        updateReferent_1.setTelephoneNumber("54654654");
        updateReferent_1.setRole("ROLE");

        return new ArrayList<>(Arrays.asList(updateReferent_0, updateReferent_1));

    }

    public static List<CreateReferent> createCreateReferentList() {
        CreateReferent createReferent_0 = new CreateReferent();
        createReferent_0.setEmailAddress("mail_a.mail@mail.com");
        createReferent_0.setFirstName("FIRSTNAME_A");
        createReferent_0.setLastName("LASTNAME_A");
        createReferent_0.setTelephoneNumber("54654654");
        createReferent_0.setRole("ROLE");

        CreateReferent createReferent_1 = new CreateReferent();
        createReferent_1.setEmailAddress("mail_b.mail@mail.com");
        createReferent_1.setFirstName("FIRSTNAME_B");
        createReferent_1.setLastName("LASTNAME_B");
        createReferent_1.setTelephoneNumber("54654654");
        createReferent_1.setRole("ROLE");

        return new ArrayList<>(Arrays.asList(createReferent_0, createReferent_1));

    }


    public static String getJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper.writeValueAsString(obj);
    }

    public static String API_TOKEN_PRIMARY_KEY = "primary-key-001";
    public static String API_TOKEN_SECONDARY_KEY = "secondary-key-001";

    public static ApiTokens createSampleApiTokens() {
        ApiTokens at = new ApiTokens();
        at.setPrimaryToken(API_TOKEN_PRIMARY_KEY);
        at.setSecondaryToken(API_TOKEN_SECONDARY_KEY);
        return at;
    }

    public static SubscriptionKeysContract createSubscriptionKeysContract() {
        return new SubscriptionKeysContractTestData(API_TOKEN_PRIMARY_KEY, API_TOKEN_SECONDARY_KEY);
    }

    public static SubscriptionContract createSubscriptionContract() {
        return new SubscriptionContractTestData(API_TOKEN_PRIMARY_KEY, API_TOKEN_SECONDARY_KEY);
    }

    public static Optional<DiscountEntity> getDiscountWithEycaUpdateId(AgreementEntity agreement) {
        DiscountEntity discount = createSampleDiscountEntityWithStaticCode(agreement, "static_code");
        discount.setEycaUpdateId("c49020231110173105078447");
        return Optional.of(discount);
    }


    public static class SubscriptionKeysContractTestData implements SubscriptionKeysContract {
        private String primaryKey;
        private String secondaryKey;

        public SubscriptionKeysContractTestData(String primaryKey, String secondaryKey) {
            this.primaryKey = primaryKey;
            this.secondaryKey = secondaryKey;
        }

        @Override
        public String primaryKey() {
            return primaryKey;
        }

        @Override
        public String secondaryKey() {
            return secondaryKey;
        }

        @Override
        public SubscriptionKeysContractInner innerModel() {
            return null;
        }
    }

    public static class SubscriptionContractTestData implements SubscriptionContract {
        private String primaryKey;
        private String secondaryKey;

        public SubscriptionContractTestData(String primaryKey, String secondaryKey) {
            this.primaryKey = primaryKey;
            this.secondaryKey = secondaryKey;
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public String type() {
            return null;
        }

        @Override
        public String ownerId() {
            return null;
        }

        @Override
        public String scope() {
            return null;
        }

        @Override
        public String displayName() {
            return null;
        }

        @Override
        public SubscriptionState state() {
            return null;
        }

        @Override
        public OffsetDateTime createdDate() {
            return null;
        }

        @Override
        public OffsetDateTime startDate() {
            return null;
        }

        @Override
        public OffsetDateTime expirationDate() {
            return null;
        }

        @Override
        public OffsetDateTime endDate() {
            return null;
        }

        @Override
        public OffsetDateTime notificationDate() {
            return null;
        }

        @Override
        public String primaryKey() {
            return primaryKey;
        }

        @Override
        public String secondaryKey() {
            return secondaryKey;
        }

        @Override
        public String stateComment() {
            return null;
        }

        @Override
        public Boolean allowTracing() {
            return null;
        }

        @Override
        public SubscriptionContractInner innerModel() {
            return null;
        }
    }

    public static HttpResponse createEmptyApimHttpResponse(int statusCode) {
        return new HttpResponse(null) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String s) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return null;
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return null;
            }
        };
    }

    public static void setOperatorAuth() {
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(new JwtOperatorUser(TestUtils.FAKE_ID,
                        TestUtils.FAKE_ID)));
    }

    public static void setAdminAuth() {
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(new JwtAdminUser(TestUtils.FAKE_ID)));
    }

    public static void printMvcResponse(ResultActions resultActions) throws UnsupportedEncodingException {
        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
}
