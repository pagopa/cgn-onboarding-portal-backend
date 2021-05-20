package it.gov.pagopa.cgn.portal;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.fluent.models.SubscriptionContractInner;
import com.azure.resourcemanager.apimanagement.fluent.models.SubscriptionKeysContractInner;
import com.azure.resourcemanager.apimanagement.implementation.SubscriptionKeysContractImpl;
import com.azure.resourcemanager.apimanagement.models.SubscriptionContract;
import com.azure.resourcemanager.apimanagement.models.SubscriptionKeysContract;
import com.azure.resourcemanager.apimanagement.models.SubscriptionState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.cgn.portal.enums.*;
import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import it.gov.pagopa.cgnonboardingportal.model.*;
import it.gov.pagopa.cgn.portal.model.*;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TestUtils {

    public static final String AGREEMENTS_CONTROLLER_PATH = "/agreements";  //needed to bypass interceptor

    private static final String AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH = AGREEMENTS_CONTROLLER_PATH + "/";

    public static final String AGREEMENT_REQUESTS_CONTROLLER_PATH = "/agreement-requests/";

    public static final String AGREEMENT_APPROVED_CONTROLLER_PATH = "/approved-agreements/";

    public static final String PUBLIC_HELP_CONTROLLER_PATH = "/help";

    public static final String FAKE_ID = "FAKE_ID";

    public static String getProfilePath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/profile";
    }

    public static String getDiscountPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/discounts";
    }

    public static String getDiscountPublishingPath(String agreementId, Long discountId) {
        return getDiscountPath(agreementId) + "/" + discountId + "/publishing";
    }

    public static String getDocumentPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/documents";
    }

    public static String getAgreementApprovalPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/approval";
    }
    public static String getBackofficeDocumentPath(String agreementId) {
        return AGREEMENT_REQUESTS_CONTROLLER_PATH + agreementId + "/documents";
    }
    public static String getAuthenticatedHelpPath(String agreementId) {
        return AGREEMENTS_CONTROLLER_PATH_PLUS_SLASH + agreementId + "/help";
    }

    public static String getAgreementRequestsWithStatusFilterPath(String state, Optional<String> assigneeOpt) {
        StringBuilder path = new StringBuilder(AGREEMENT_REQUESTS_CONTROLLER_PATH);
        path.append("?states=").append(state);
        assigneeOpt.ifPresent(assignee -> path.append("&assignee=").append(assignee));
        return path.toString();
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
        profileEntity.setTaxCodeOrVat("abcdeghilmnopqrs");
        profileEntity.setPecAddress("pec.address@pagopa.it");
        profileEntity.setDescription("A Description");
        profileEntity.setReferent(createSampleReferent(profileEntity));
        profileEntity.setLegalRepresentativeTaxCode("abcdeghilmnopqrs");
        profileEntity.setLegalRepresentativeFullName("full name");
        profileEntity.setLegalOffice("legal office");
        profileEntity.setDiscountCodeType(discountCodeType);
        profileEntity.setTelephoneNumber("12345678");
        return profileEntity;
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

    public static ProfileEntity createSampleProfileEntity(AgreementEntity agreementEntity) {
        return createSampleProfileEntity(agreementEntity, SalesChannelEnum.ONLINE, DiscountCodeTypeEnum.STATIC);
    }

    public static ProfileEntity createSampleProfileEntity(AgreementEntity agreementEntity, SalesChannelEnum salesChannel, DiscountCodeTypeEnum discountCodeType) {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields(discountCodeType);
        profileEntity.setWebsiteUrl("https://www.pagopa.gov.it/");
        profileEntity.setSalesChannel(salesChannel);
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


    public static DiscountEntity createSampleDiscountEntity(AgreementEntity agreement) {
        DiscountEntity discountEntity = createSampleDiscountEntityWithoutProduct(agreement);
        discountEntity.setProducts(getProductEntityList(discountEntity));
        return discountEntity;
    }

    public static DiscountEntity createSampleDiscountEntityWithoutProduct(AgreementEntity agreement) {
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
        return discountEntity;
    }

    public static List<DiscountProductEntity> getProductEntityList(DiscountEntity discountEntity) {
        List<DiscountProductEntity> productEntityList = new ArrayList<>();
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.TRAVELS);
        productEntityList.add(productEntity);
        productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.SPORTS);
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

    public static List<DocumentEntity> createSampleBackofficeDocumentList(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = new ArrayList<>();
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.BACKOFFICE_AGREEMENT));
        documentList.add(createDocument(agreementEntity, DocumentTypeEnum.BACKOFFICE_MANIFESTATION_OF_INTEREST));
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

    public static class SubscriptionKeysContractTestData implements SubscriptionKeysContract {
        private String primaryKey;
        private String secondaryKey;

        public SubscriptionKeysContractTestData(String primaryKey, String secondaryKey) {
            this.primaryKey = primaryKey;
            this.secondaryKey = secondaryKey;
        }

        @Override public String primaryKey() { return primaryKey; }
        @Override public String secondaryKey() { return secondaryKey; }
        @Override public SubscriptionKeysContractInner innerModel() { return null; }
    }

    public static class SubscriptionContractTestData implements SubscriptionContract {
        private String primaryKey;
        private String secondaryKey;

        public SubscriptionContractTestData(String primaryKey, String secondaryKey) {
            this.primaryKey = primaryKey;
            this.secondaryKey = secondaryKey;
        }

        @Override public String id() { return null; }
        @Override public String name() { return null; }
        @Override public String type() { return null; }
        @Override public String ownerId() { return null; }
        @Override public String scope() { return null; }
        @Override public String displayName() { return null; }
        @Override public SubscriptionState state() { return null; }
        @Override public OffsetDateTime createdDate() { return null; }
        @Override public OffsetDateTime startDate() { return null; }
        @Override public OffsetDateTime expirationDate() { return null; }
        @Override public OffsetDateTime endDate() { return null; }
        @Override public OffsetDateTime notificationDate() { return null; }
        @Override public String primaryKey() { return primaryKey; }
        @Override public String secondaryKey() { return secondaryKey; }
        @Override public String stateComment() { return null; }
        @Override public Boolean allowTracing() { return null; }
        @Override public SubscriptionContractInner innerModel() { return null; }
    }

    public static HttpResponse createEmptyApimHttpResponse(int statusCode) {
        return new HttpResponse(null) {
            @Override public int getStatusCode() { return statusCode; }
            @Override public String getHeaderValue(String s) { return null; }
            @Override public HttpHeaders getHeaders() { return null; }
            @Override public Flux<ByteBuffer> getBody() { return null; }
            @Override public Mono<byte[]> getBodyAsByteArray() { return null; }
            @Override public Mono<String> getBodyAsString() { return null; }
            @Override public Mono<String> getBodyAsString(Charset charset) { return null; }
        };
    }


    public static void setOperatorAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new JwtOperatorUser(TestUtils.FAKE_ID, TestUtils.FAKE_ID))
        );
    }

    public static void setAdminAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new JwtAdminUser(TestUtils.FAKE_ID))
        );
    }
}
