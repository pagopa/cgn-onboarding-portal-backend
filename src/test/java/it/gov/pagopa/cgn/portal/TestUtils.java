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
import it.gov.pagopa.cgnonboardingportal.backoffice.model.SuspendDiscount;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
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

    public static final String FAKE_ORGANIZATION_NAME = "FAKE_ORGANIZATION_NAME";

    public static final String API_TOKEN_PRIMARY_KEY = "primary-key-001";
    public static final String API_TOKEN_SECONDARY_KEY = "secondary-key-001";

    public static final String FAKE_OID_1 = "c75020241204020019952562";
    public static final String FAKE_OID_2 = "c28020241204020019770616";
    public static final String FAKE_OID_3 = "c28020241204020019770611";

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

    public static String getAgreementRequestApprovalPath(String agreementId) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH + agreementId + "/approval";
    }

    public static String getAgreementRequestAssigneePath(String agreementId) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH + agreementId + "/assignee";
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

    public static String getAgreementRequestsDiscountTestingPath(String agreementId, String discountId) {
        return getDiscountPath(agreementId) + "/" + discountId + "/testing";
    }

    public static String getAgreementRequestsDiscountSuspendingPath(String agreementId, String discountId) {
        return getAgreementRequestsDiscountPath(agreementId, discountId) + "/suspension";
    }


    public static String getAgreementRequestsWithSortedColumn(BackofficeRequestSortColumnEnum columnEnum,
                                                              Sort.Direction direction) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH + "?sortColumn=" + columnEnum.getValue() + "&sortDirection=" +
               direction.name();
    }

    public static String getAgreementApprovalWithSortedColumn(BackofficeApprovedSortColumnEnum columnEnum,
                                                              Sort.Direction direction) {
        return AGREEMENT_APPROVED_CONTROLLER_PATH + "?sortColumn=" + columnEnum.getValue() + "&sortDirection=" +
               direction.name();
    }

    public static String createAgreements() {
        return AGREEMENTS_CONTROLLER_PATH;
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
        salesChannel.setAddresses(Stream.of(address).toList());
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
        salesChannel.setAddresses(Stream.of(address).toList());
        salesChannel.setAllNationalAddresses(true);
        return createProfileFromProfileEntity(profileEntity, salesChannel);
    }


    public static CreateProfile createProfileFromProfileEntity(ProfileEntity profileEntity, SalesChannel salesChannel) {
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

        SecondaryReferentEntity secondaryReferentEntity0 = new SecondaryReferentEntity();
        secondaryReferentEntity0.setFirstName("FIRST_NAME_0");
        secondaryReferentEntity0.setLastName("LAST_NAME_0");
        secondaryReferentEntity0.setEmailAddress("referent.registry_0@pagopa.it");
        secondaryReferentEntity0.setTelephoneNumber("+390123456789");
        secondaryReferentEntity0.setProfile(profileEntity);
        secondaryReferentEntity0.setRole("CEO");

        SecondaryReferentEntity secondaryReferentEntity1 = new SecondaryReferentEntity();
        secondaryReferentEntity1.setFirstName("FIRST_NAME_1");
        secondaryReferentEntity1.setLastName("LAST_NAME_1");
        secondaryReferentEntity1.setEmailAddress("referent.registry_1@pagopa.it");
        secondaryReferentEntity1.setTelephoneNumber("+390123456789");
        secondaryReferentEntity1.setProfile(profileEntity);
        secondaryReferentEntity1.setRole("CEO");

        return new ArrayList<>(Arrays.asList(secondaryReferentEntity0, secondaryReferentEntity1));
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
        it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest helpRequest = new it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest();
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
        it.gov.pagopa.cgnonboardingportal.model.HelpRequest helpRequest = new it.gov.pagopa.cgnonboardingportal.model.HelpRequest();
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
        discountEntity.setEycaLandingPageUrl(null);
        discountEntity.setLandingPageReferrer(null);
        discountEntity.setDiscountUrl("https://anurl.com");
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithLandingPage(AgreementEntity agreement,
                                                                           String url,
                                                                           String eycaUrl,
                                                                           String referrer) {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreement);
        discountEntity.setStaticCode(null);
        discountEntity.setLandingPageUrl(url);
        discountEntity.setEycaLandingPageUrl(eycaUrl);
        discountEntity.setLandingPageReferrer(referrer);
        discountEntity.setVisibleOnEyca(!StringUtils.isEmpty(eycaUrl));
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithBucketCodes(AgreementEntity agreement) {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreement);
        discountEntity.setStaticCode(null);
        discountEntity.setLastBucketCodeLoadUid(generateDiscountBucketCodeUid());
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithEycaLandingPage(AgreementEntity agreement) {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreement);
        discountEntity.setEycaLandingPageUrl("https://www.contoso.com/lpe");
        discountEntity.setVisibleOnEyca(true);

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
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(1L);
        entity0.setCategories("products");
        entity0.setProfileId(1L);
        entity0.setVendor("vendor_0");
        entity0.setName("name_0");
        entity0.setNameLocal("name_local_0");
        entity0.setStreet("address0");
        entity0.setDiscountType("LANDING PAGE");
        entity0.setReferent(1L);
        entity0.setLive("Y");
        entity0.setDiscountId(7L);
        entity0.setLandingPageUrl("LANDING PAGE URL");
        entity0.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity0.setEycaEmailUpdateRequired(true);
        return Collections.singletonList(entity0);

    }


    private static EycaDataExportViewEntity getRealEycaDataExportViewEntity0(Long discountId, String eycaUpdateId) {
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(40L);
        entity0.setCategories("SV");
        entity0.setProfileId(134L);
        entity0.setDiscountId(discountId);
        entity0.setReferent(134L);
        entity0.setEycaUpdateId(eycaUpdateId);
        entity0.setVendor("Dipartimento per le Politiche giovanili e il Servizio civile universale");
        entity0.setName("-");
        entity0.setNameLocal("OpportunitÃ  di Stage con Milano Premier Padel P1");
        entity0.setText("- - - - To access the discount, show your EYCA card at the point of sale.");
        entity0.setTextLocal(
                "Durante l'evento,allo stand del Ministro per lo Sport e i Giovani,Ã¨ possibile presentare il proprio CV per uno stage di 6 mesi nell'organizzazione di eventi sportivi internazionali.E' previsto un rimborso spese mensile di â‚¬400 per 20 ore a settimana. - Necessaria l'iscrizione a un corso di laurea specialistica,master o dottorato,oltre a un eccellente livello di inglese. Requisiti e ulteriori informazioni al link. - Per accedere all'agevolazione, mostra la tua carta EYCA presso il punto vendita.");
        entity0.setStartDate(LocalDate.of(2023, 11, 29));
        entity0.setEndDate(LocalDate.of(2023, 12, 10));
        entity0.setEmail("");
        entity0.setPhone("");
        entity0.setWeb(null);
        entity0.setTags("");
        entity0.setImage(
                "https://cgnonboardingportal-p-cdnendpoint-storage.azureedge.net/profileimages/image-2eb38226-928f-40e7-8a06-eecb4a15cb1f.jpg");
        entity0.setLocationLocalId("");
        entity0.setStreet(null);
        entity0.setCity("");
        entity0.setZip("");
        entity0.setCountry("");
        entity0.setRegion("");
        entity0.setLongitude("");
        entity0.setLatitude("");
        entity0.setDiscountType("SHOP");
        entity0.setLive("Y");

        return entity0;
    }


    private static EycaDataExportViewEntity getRealEycaDataExportViewEntity1(Long discountId, String eycaUpdateId) {
        EycaDataExportViewEntity entity1 = new EycaDataExportViewEntity();
        entity1.setId(26L);
        entity1.setCategories("LR,SV");
        entity1.setProfileId(134L);
        entity1.setDiscountId(discountId);
        entity1.setReferent(134L);
        entity1.setEycaUpdateId(eycaUpdateId);
        entity1.setVendor("Dipartimento per le Politiche giovanili e il Servizio civile universale");
        entity1.setName("Giovani2030");
        entity1.setNameLocal("Giovani2030");
        entity1.setText(
                "is the digital home created by Dipartimento per le politiche giovanili e il Servizio civile universale for those willing to get new tools, face new challenges and find their own way. - If you are between 14 and 35 years old, you are an italian resident and you want to get opportunities for education, volunteering, national and foreign calls, GIOVANI2030 is the right place for you! - To access the discount, show your EYCA card at the point of sale.");
        entity1.setTextLocal(
                "la casa digitale creata dal Dipartimento per le politiche giovanili e il Servizio civile universale proprio per chi, come te, cerca nuovi strumenti e nuove sfide per crescere e trovare la propria strada. - Se hai tra i 14 e i 35 anni, risiedi in Italia e vuoi conoscere le migliori opportunitÃ  di formazione, volontariato, cultura e bandi nazionali ed esteri, GIOVANI2030 Ã¨ il posto giusto per te! - Per accedere all'agevolazione, mostra la tua carta EYCA presso il punto vendita.");
        entity1.setStartDate(LocalDate.of(2023, 4, 17));
        entity1.setEndDate(LocalDate.of(2023, 12, 31));
        entity1.setEmail("");
        entity1.setPhone("");
        entity1.setWeb(null);
        entity1.setTags("");
        entity1.setImage(
                "https://cgnonboardingportal-p-cdnendpoint-storage.azureedge.net/profileimages/image-2eb38226-928f-40e7-8a06-eecb4a15cb1f.jpg");
        entity1.setLocationLocalId("");
        entity1.setStreet(null);
        entity1.setCity("");
        entity1.setZip("");
        entity1.setCountry("");
        entity1.setRegion("");
        entity1.setLongitude("");
        entity1.setLatitude("");
        entity1.setDiscountType("SHOP");
        entity1.setLive("Y");

        return entity1;
    }


    public static List<EycaDataExportViewEntity> getRealDataList() {
        return Arrays.asList(getRealEycaDataExportViewEntity0(500L, null),
                             getRealEycaDataExportViewEntity1(501L, null),
                             getRealEycaDataExportViewEntity0(502L, "c49020231110173105078447"),
                             getRealEycaDataExportViewEntity1(503L, "c49020232220173105078447"));
    }

    public static List<EycaDataExportViewEntity> getRealDataListForSync() {
        return List.of(getRealEycaDataExportViewEntity0(502L, "c49020231110173105078447"));
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportViewEntityListFromCSV() {
        return getEycaDataExportViewEntityListFromCSV(TestUtils.class.getClassLoader()
                                                                     .getResourceAsStream("eyca_data_export.csv"));
    }


    public static List<EycaDataExportViewEntity> getEycaDataExportViewEntityListFromCSV(InputStream is) {
        return CsvUtils.csvToEntityList(is, record -> {
            EycaDataExportViewEntity e = new EycaDataExportViewEntity();

            e.setId(Long.valueOf(record.get("id")));
            e.setCategories(record.get("categories"));
            e.setProfileId(Long.valueOf(record.get("profile_id")));
            e.setVendor(record.get("vendor"));
            e.setDiscountId(Long.valueOf(record.get("discount_id")));
            e.setEycaUpdateId(record.get("eyca_update_id"));
            e.setName(record.get("name"));
            e.setStartDate(LocalDate.parse(record.get("start_date")));
            e.setEndDate(LocalDate.parse(record.get("end_date")));
            e.setNameLocal(record.get("name_local"));
            e.setText(record.get("text"));
            e.setTextLocal(record.get("text_local"));
            e.setEmail(record.get("email"));
            e.setPhone(record.get("phone"));
            e.setWeb(record.get("web"));
            e.setTags(record.get("tags"));
            e.setImage(record.get("image"));
            e.setLive(record.get("live"));
            e.setLocationLocalId(record.get("location_local_id"));
            e.setStreet(record.get("street"));
            e.setCity(record.get("city"));
            e.setZip(record.get("zip"));
            e.setCountry(record.get("country"));
            e.setRegion(record.get("region"));
            e.setLatitude(record.get("latitude"));
            e.setLongitude(record.get("longitude"));
            e.setDiscountType(record.get("discount_type"));
            e.setStaticCode(record.get("static_code"));
            e.setLandingPageUrl(record.get("landing_page_url"));
            e.setLandingPageReferrer(record.get("landing_page_referrer"));
            e.setReferent(Long.valueOf(record.get("referent")));
            e.setEycaLandingPageUrl(record.get("eyca_landing_page_url"));
            e.setEycaEmailUpdateRequired(Boolean.valueOf(record.get("eyca_email_update_required")));
            return e;
        });
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportForCreate() {
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(1L);
        entity0.setCategories("products");
        entity0.setEycaUpdateId("");
        entity0.setProfileId(1L);
        entity0.setVendor("vendor_0");
        entity0.setName("name_0");
        entity0.setNameLocal("name_local_0");
        entity0.setStreet("address0");
        entity0.setDiscountType("LANDING PAGE");
        entity0.setLive("Y");
        entity0.setDiscountId(1L);
        entity0.setLandingPageUrl("LANDING PAGE URL");
        entity0.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity0.setEycaEmailUpdateRequired(true);
        return List.of(entity0);
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportForUpdate() {
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(1L);
        entity0.setCategories("products");
        entity0.setEycaUpdateId("1234");
        entity0.setProfileId(1L);
        entity0.setVendor("vendor_0");
        entity0.setName("name_0");
        entity0.setNameLocal("name_local_0");
        entity0.setStreet("address0");
        entity0.setDiscountType("LANDING PAGE");
        entity0.setLive("Y");
        entity0.setDiscountId(1L);
        entity0.setLandingPageUrl("LANDING PAGE URL");
        entity0.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity0.setEycaEmailUpdateRequired(true);
        return List.of(entity0);
    }

    public static List<EycaDataExportViewEntity> getEycaDataExportViewEntityList() {
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(1L);
        entity0.setCategories("products");
        entity0.setProfileId(1L);
        entity0.setVendor("vendor_0");
        entity0.setName("name_0");
        entity0.setNameLocal("name_local_0");
        entity0.setStreet("address0");
        entity0.setDiscountType("LANDING PAGE");
        entity0.setLive("Y");
        entity0.setLandingPageUrl("LANDING PAGE URL");
        entity0.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity0.setEycaEmailUpdateRequired(true);
        entity0.setDiscountId(1L);

        EycaDataExportViewEntity entity1 = new EycaDataExportViewEntity();
        entity1.setId(1L);
        entity1.setCategories("products");
        entity1.setProfileId(1L);
        entity1.setVendor("vendor_0");
        entity1.setName("name_0");
        entity1.setNameLocal("name_local_0");
        entity1.setDiscountType("LANDING PAGE");
        entity1.setLive("Y");
        entity1.setDiscountId(2L);
        entity1.setLandingPageUrl("LANDING PAGE URL");
        entity1.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity1.setCountry("italy");
        entity1.setCity("city");
        entity1.setStreet("address0");
        entity1.setLatitude("48");
        entity1.setLongitude("12");
        entity1.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity2 = new EycaDataExportViewEntity();
        entity2.setId(1L);
        entity2.setCategories("products");
        entity2.setProfileId(1L);
        entity2.setVendor("vendor_0");
        entity2.setName("name_0");
        entity2.setNameLocal("name_local_0");
        entity2.setDiscountType("LANDING PAGE");
        entity2.setLive("Y");
        entity2.setDiscountId(3L);
        entity2.setLandingPageUrl("LANDING PAGE URL");
        entity2.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity2.setCity("city");
        entity2.setStreet("address0");
        entity2.setLatitude("48");
        entity2.setLongitude("12");
        entity2.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity3 = new EycaDataExportViewEntity();
        entity3.setId(1L);
        entity3.setCategories("products");
        entity3.setProfileId(1L);
        entity3.setVendor("vendor_0");
        entity3.setName("name_0");
        entity3.setNameLocal("name_local_0");
        entity3.setDiscountType("LANDING PAGE");
        entity3.setLive("Y");
        entity3.setDiscountId(4L);
        entity3.setLandingPageUrl("LANDING PAGE URL");
        entity3.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity3.setCountry("italy");
        entity3.setStreet("address0");
        entity3.setLatitude("48");
        entity3.setLongitude("12");
        entity3.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity4 = new EycaDataExportViewEntity();
        entity4.setId(1L);
        entity4.setCategories("products");
        entity4.setProfileId(1L);
        entity4.setVendor("vendor_0");
        entity4.setName("name_0");
        entity4.setNameLocal("name_local_0");
        entity4.setDiscountType("LANDING PAGE");
        entity4.setLive("Y");
        entity4.setDiscountId(5L);
        entity4.setLandingPageUrl("LANDING PAGE URL");
        entity4.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity4.setCountry("italy");
        entity4.setCity("city");
        entity4.setLatitude("48");
        entity4.setLongitude("12");
        entity4.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity5 = new EycaDataExportViewEntity();
        entity5.setId(1L);
        entity5.setCategories("products");
        entity5.setProfileId(1L);
        entity5.setVendor("vendor_0");
        entity5.setName("name_0");
        entity5.setNameLocal("name_local_0");
        entity5.setDiscountType("LANDING PAGE");
        entity5.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity5.setLive("Y");
        entity5.setDiscountId(6L);
        entity5.setLandingPageUrl("LANDING PAGE URL");
        entity5.setCountry("italy");
        entity5.setCity("city");
        entity5.setStreet("address0");
        entity5.setLongitude("12");
        entity5.setTags("tag1, tag2");
        entity5.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity6 = new EycaDataExportViewEntity();
        entity6.setId(1L);
        entity6.setCategories("products");
        entity6.setProfileId(1L);
        entity6.setVendor("vendor_0");
        entity6.setName("name_0");
        entity6.setNameLocal("name_local_0");
        entity6.setStreet("address0");
        entity6.setDiscountType("mode0");
        entity6.setDiscountId(7L);
        entity6.setLive(null);
        entity6.setEndDate(LocalDate.now().minusDays(4));

        EycaDataExportViewEntity entity7 = new EycaDataExportViewEntity();
        entity7.setId(2L);
        entity7.setCategories("products");
        entity7.setProfileId(1L);
        entity7.setVendor("vendor_0");
        entity7.setName("name_0");
        entity7.setNameLocal("name_local_0");
        entity7.setStreet("address0");
        entity7.setDiscountType("mode0");
        entity7.setLive("N");
        entity7.setDiscountId(8L);
        entity7.setEndDate(LocalDate.now().minusDays(2));

        EycaDataExportViewEntity entity8 = new EycaDataExportViewEntity();
        entity8.setId(1L);
        entity8.setCategories("products");
        entity8.setProfileId(1L);
        entity8.setVendor("vendor_0");
        entity8.setName("name_0");
        entity8.setNameLocal("name_local_0");
        entity8.setStreet("address0");
        entity8.setDiscountType("LANDING PAGE");
        entity8.setLive("y");
        entity8.setDiscountId(9L);
        entity8.setLandingPageUrl("LANDING PAGE URL");
        entity8.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity8.setEycaUpdateId("655464565");
        entity8.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity9 = new EycaDataExportViewEntity();
        entity9.setId(1L);
        entity9.setCategories("products");
        entity9.setProfileId(1L);
        entity9.setVendor("vendor_1");
        entity9.setName("name_1");
        entity9.setNameLocal("name_local_1");
        entity9.setStreet("address1");
        entity9.setDiscountType("LANDING PAGE");
        entity9.setLive("N");
        entity9.setDiscountId(10L);
        entity9.setEycaUpdateId("650054665");
        entity9.setLandingPageUrl("LANDING PAGE URL");
        entity9.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity9.setEndDate(LocalDate.now().minusDays(4));
        entity9.setEycaEmailUpdateRequired(true);
        EycaDataExportViewEntity entity10 = new EycaDataExportViewEntity();

        entity10.setId(1L);
        entity10.setCategories("products");
        entity10.setProfileId(1L);
        entity10.setVendor("vendor_2");
        entity10.setName("name_2");
        entity10.setNameLocal("name_local_2");
        entity10.setStreet("address2");
        entity10.setDiscountType("LANDING PAGE");
        entity10.setLive("N");
        entity10.setDiscountId(10L);
        entity10.setLandingPageUrl("LANDING PAGE URL");
        entity10.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity10.setEycaUpdateId("6551114565");
        entity10.setEndDate(LocalDate.now());
        entity10.setEycaEmailUpdateRequired(true);
        EycaDataExportViewEntity entity11 = new EycaDataExportViewEntity();

        entity11.setId(1L);
        entity11.setCategories("products");
        entity11.setProfileId(1L);
        entity11.setVendor("vendor_2");
        entity11.setName("name_2");
        entity11.setNameLocal("name_local_2");
        entity11.setStreet("address2");
        entity11.setDiscountType("LANDING PAGE");
        entity11.setLive("Y");
        entity11.setDiscountId(11L);
        entity11.setLandingPageUrl("LANDING PAGE URL");
        entity11.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity11.setEycaUpdateId("6551114565");
        entity11.setEndDate(LocalDate.now().plusDays(2));

        EycaDataExportViewEntity entity12 = new EycaDataExportViewEntity();
        entity12.setId(1L);
        entity12.setCategories("products");
        entity12.setProfileId(1L);
        entity12.setVendor("vendor_0");
        entity12.setName("name_0");
        entity12.setNameLocal("name_local_0");
        entity12.setStreet("address0");
        entity12.setLive("Y");
        entity12.setDiscountId(12L);
        entity12.setEycaUpdateId("655464565");

        EycaDataExportViewEntity entity13 = new EycaDataExportViewEntity();
        entity13.setId(1L);
        entity13.setCategories("products");
        entity13.setProfileId(1L);
        entity13.setVendor("vendor_0");
        entity13.setName("name_0");
        entity13.setNameLocal("name_local_0");
        entity13.setStreet("address0");
        entity13.setLive("Y");
        entity13.setDiscountId(13L);
        entity13.setDiscountType("LIST OF STATIC CODES");

        EycaDataExportViewEntity entity14 = new EycaDataExportViewEntity();
        entity14.setId(1L);
        entity14.setCategories("products");
        entity14.setProfileId(1L);
        entity14.setVendor("vendor_0");
        entity14.setName("name_0");
        entity14.setNameLocal("name_local_0");
        entity14.setStreet("address0");
        entity14.setDiscountType("mode0");
        entity14.setLive("Y");
        entity14.setDiscountId(14L);

        EycaDataExportViewEntity entity15 = new EycaDataExportViewEntity();
        entity15.setId(1L);
        entity15.setCategories("products");
        entity15.setProfileId(1L);
        entity15.setVendor("vendor_0");
        entity15.setName("name_0");
        entity15.setNameLocal("name_local_0");
        entity15.setDiscountType("LIST OF STATIC CODES");
        entity15.setLive("Y");
        entity15.setDiscountId(15L);

        EycaDataExportViewEntity entity16 = new EycaDataExportViewEntity();
        entity16.setId(1L);
        entity16.setCategories("products");
        entity16.setEycaUpdateId("hughgt7y98565");
        entity16.setProfileId(1L);
        entity16.setVendor("vendor_0");
        entity16.setName("name_0");
        entity16.setNameLocal("name_local_0");
        entity16.setDiscountType("LIST OF STATIC CODES");
        entity16.setLive("Y");
        entity16.setDiscountId(16L);


        return Arrays.asList(entity0,
                             entity1,
                             entity2,
                             entity3,
                             entity4,
                             entity5,
                             entity6,
                             entity7,
                             entity8,
                             entity9,
                             entity10,
                             entity11,
                             entity12,
                             entity13,
                             entity14,
                             entity15,
                             entity16);
    }


    public static List<EycaDataExportViewEntity> getTobeDeletedEycaDataExportViewEntityList() {
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(1L);
        entity0.setCategories("products");
        entity0.setProfileId(1L);
        entity0.setVendor("vendor_0");
        entity0.setName("name_0");
        entity0.setNameLocal("name_local_0");
        entity0.setStreet("address0");
        entity0.setDiscountType("LANDING PAGE");
        entity0.setLive("N");
        entity0.setDiscountId(6L);
        entity0.setLandingPageUrl("LANDING PAGE URL");
        entity0.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity0.setEycaUpdateId("ce00958658596");
        entity0.setStartDate(LocalDate.now().minusDays(10));
        entity0.setEndDate(LocalDate.now());
        entity0.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity1 = new EycaDataExportViewEntity();
        entity1.setId(2L);
        entity1.setCategories("products");
        entity1.setProfileId(1L);
        entity1.setVendor("vendor_0");
        entity1.setName("name_0");
        entity1.setNameLocal("name_local_0");
        entity1.setDiscountType("LANDING PAGE");
        entity1.setLive(null);
        entity1.setStartDate(LocalDate.now().minusDays(10));
        entity1.setEndDate(LocalDate.now());
        entity1.setDiscountId(7L);
        entity1.setLandingPageUrl("LANDING PAGE URL");
        entity1.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity1.setEycaUpdateId("ce00957778596");
        entity1.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity2 = new EycaDataExportViewEntity();
        entity2.setId(1L);
        entity2.setCategories("products");
        entity2.setProfileId(1L);
        entity2.setVendor("vendor_0");
        entity2.setName("name_0");
        entity2.setNameLocal("name_local_0");
        entity2.setDiscountType("LANDING PAGE");
        entity2.setLive("N");
        entity2.setStartDate(LocalDate.now().minusDays(10));
        entity2.setEndDate(LocalDate.now());
        entity2.setDiscountId(8L);
        entity2.setLandingPageUrl("LANDING PAGE URL");
        entity2.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity2.setEycaUpdateId("ce00958999596");
        entity2.setEycaEmailUpdateRequired(true);

        EycaDataExportViewEntity entity3 = new EycaDataExportViewEntity();
        entity3.setId(1L);
        entity3.setCategories("products");
        entity3.setProfileId(1L);
        entity3.setVendor("vendor_0");
        entity3.setName("name_0");
        entity3.setNameLocal("name_local_0");
        entity3.setDiscountType("LANDING PAGE");
        entity3.setLive("N");
        entity3.setStartDate(LocalDate.now().minusDays(10));
        entity3.setEndDate(LocalDate.now());
        entity3.setDiscountId(8L);
        entity3.setLandingPageUrl("LANDING PAGE URL");
        entity3.setEycaLandingPageUrl("EYCA LANDING PAGE URL");
        entity3.setEycaEmailUpdateRequired(true);

        return Arrays.asList(entity0, entity1, entity2, entity3);

    }

    public static List<EycaDataExportViewEntity> getListWIthNoDiscountype() {
        EycaDataExportViewEntity entity0 = new EycaDataExportViewEntity();
        entity0.setId(1L);
        entity0.setCategories("products");
        entity0.setProfileId(1L);
        entity0.setVendor("vendor_0");
        entity0.setName("name_0");
        entity0.setNameLocal("name_local_0");
        entity0.setStreet("address0");
        entity0.setLive("Y");
        entity0.setDiscountId(7L);
        entity0.setEycaUpdateId("655464565");

        return Collections.singletonList(entity0);
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
        updateDiscount.setEycaLandingPageUrl(discount.getEycaLandingPageUrl());
        updateDiscount.setProductCategories(discount.getProductCategories());
        updateDiscount.setLastBucketCodeLoadUid(discount.getLastBucketCodeLoadUid());
        updateDiscount.setLastBucketCodeLoadFileName(discount.getLastBucketCodeLoadFileName());

        return updateDiscount;
    }

    public static SuspendDiscount suspendableDiscountFromDiscountEntity(DiscountEntity discountEntity) {
        SuspendDiscount suspendDiscount = new SuspendDiscount();
        suspendDiscount.setReasonMessage("fake reason");
        return suspendDiscount;
    }

    public static SearchApiResponseEyca getSearchApiResponseEyca() {
        SearchApiResponseEyca searchApiResponseEyca = new SearchApiResponseEyca();
        SearchApiResponseApiResponseEyca searchApiResponseApiResponseEyca = new SearchApiResponseApiResponseEyca();
        SearchApiResponseApiResponseDataEyca searchApiResponseApiResponseDataEyca = new SearchApiResponseApiResponseDataEyca();
        SearchApiResponseApiResponseDataDiscountsEyca searchApiResponseApiResponseDataDiscountsEyca = new SearchApiResponseApiResponseDataDiscountsEyca();

        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId(FAKE_OID_1);
        items.add(discountItemEyca);

        searchApiResponseApiResponseEyca.setData(searchApiResponseApiResponseDataEyca);
        searchApiResponseApiResponseDataEyca.setDiscounts(searchApiResponseApiResponseDataDiscountsEyca);
        searchApiResponseApiResponseDataDiscountsEyca.setData(items);
        searchApiResponseEyca.setApiResponse(searchApiResponseApiResponseEyca);
        return searchApiResponseEyca;
    }

    public static SearchApiResponseEyca getSearchApiResponseWithDataEmptyList() {

        SearchApiResponseEyca searchApiResponseEyca = new SearchApiResponseEyca();
        SearchApiResponseApiResponseEyca searchApiResponseApiResponseEyca = new SearchApiResponseApiResponseEyca();
        SearchApiResponseApiResponseDataEyca searchApiResponseApiResponseDataEyca = new SearchApiResponseApiResponseDataEyca();
        SearchApiResponseApiResponseDataDiscountsEyca searchApiResponseApiResponseDataDiscountsEyca = new SearchApiResponseApiResponseDataDiscountsEyca();

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
        discountItemEyca.setId(FAKE_OID_1);

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

    public static DeleteApiResponseEyca getDeleteApiResponseInternalServerError() {
        DeleteApiResponseEyca apiResponseEyca = new DeleteApiResponseEyca();

        DeleteApiResponseApiResponseEyca deleteApiResponseApiResponseEyca = new DeleteApiResponseApiResponseEyca();
        deleteApiResponseApiResponseEyca.setError(1);
        deleteApiResponseApiResponseEyca.setCode(4);
        deleteApiResponseApiResponseEyca.setText(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        deleteApiResponseApiResponseEyca.setData(null);
        deleteApiResponseApiResponseEyca.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
        UpdateReferent updateReferent0 = new UpdateReferent();
        updateReferent0.setEmailAddress("mail_a.mail@mail.com");
        updateReferent0.setFirstName("FIRSTNAME_A");
        updateReferent0.setLastName("LASTNAME_A");
        updateReferent0.setTelephoneNumber("54654654");
        updateReferent0.setRole("ROLE");

        UpdateReferent updateReferent1 = new UpdateReferent();
        updateReferent1.setEmailAddress("mail_b.mail@mail.com");
        updateReferent1.setFirstName("FIRSTNAME_B");
        updateReferent1.setLastName("LASTNAME_B");
        updateReferent1.setTelephoneNumber("54654654");
        updateReferent1.setRole("ROLE");

        return new ArrayList<>(Arrays.asList(updateReferent0, updateReferent1));

    }

    public static List<CreateReferent> createCreateReferentList() {
        CreateReferent createReferent0 = new CreateReferent();
        createReferent0.setEmailAddress("mail_a.mail@mail.com");
        createReferent0.setFirstName("FIRSTNAME_A");
        createReferent0.setLastName("LASTNAME_A");
        createReferent0.setTelephoneNumber("54654654");
        createReferent0.setRole("ROLE");

        CreateReferent createReferent1 = new CreateReferent();
        createReferent1.setEmailAddress("mail_b.mail@mail.com");
        createReferent1.setFirstName("FIRSTNAME_B");
        createReferent1.setLastName("LASTNAME_B");
        createReferent1.setTelephoneNumber("54654654");
        createReferent1.setRole("ROLE");

        return new ArrayList<>(Arrays.asList(createReferent0, createReferent1));

    }


    public static String getJson(Object obj)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper.writeValueAsString(obj);
    }


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

    public static ListApiResponseEyca getListApiResponseEyca() {
        ListApiResponseEyca response = new ListApiResponseEyca();
        ListApiResponseApiResponseEyca listApiResponseApiResponseEyca = new ListApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseApiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> itemsFromEyca = new ArrayList<>();
        DiscountItemEyca item1 = new DiscountItemEyca();
        item1.setId(FAKE_OID_1);
        DiscountItemEyca item2 = new DiscountItemEyca();
        item2.setId(FAKE_OID_2);
        itemsFromEyca.add(item1);
        itemsFromEyca.add(item2);
        apiResponseApiResponseDataEyca.setDiscount(itemsFromEyca);
        listApiResponseApiResponseEyca.setData(apiResponseApiResponseDataEyca);
        response.setApiResponse(listApiResponseApiResponseEyca);
        return response;
    }


    public static class SubscriptionKeysContractTestData
            implements SubscriptionKeysContract {
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

    public static class SubscriptionContractTestData
            implements SubscriptionContract {
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

    public static SearchDataExportEyca createEmptySearchDataExportEyca() {
        return new SearchDataExportEyca();
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
}