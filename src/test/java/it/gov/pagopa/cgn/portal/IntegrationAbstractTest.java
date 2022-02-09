package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.service.*;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ContextConfiguration(initializers = IntegrationAbstractTest.Initializer.class)
@Slf4j
public class IntegrationAbstractTest {

    protected String getAzureConnectionString() {
        return "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
                + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
                + "BlobEndpoint=http://127.0.0.1:" + Initializer.azurite.getMappedPort(10000) + "/devstoreaccount1;";
    }

    protected static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static JdbcDatabaseContainer<?> postgres = new PostgisContainerProvider().newInstance("11-2.5")
                .withDatabaseName("integration-tests-db").withUsername("admin").withPassword("admin");

        public static GenericContainer<?> azurite = new GenericContainer<>(
                DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.11.0")).withExposedPorts(10000);

        public static GenericContainer<?> greenMailContainer = new GenericContainer<>(
                DockerImageName.parse("greenmail/standalone:1.6.3")).withExposedPorts(3025)
                // override timeout to 5 seconds
                .withEnv("GREENMAIL_OPTS",
                        "-Dgreenmail.setup.test.all -Dgreenmail.hostname=0.0.0.0 -Dgreenmail.auth.disabled -Dgreenmail.startup.timeout=5000");

        private static void startContainers() {
            Startables.deepStart(Stream.of(postgres, azurite, greenMailContainer)).join();
        }

        private static Map<String, String> createConnectionConfiguration() {
            return Map.of("spring.datasource.url", postgres.getJdbcUrl(), "spring.datasource.username",
                    postgres.getUsername(), "spring.datasource.password", postgres.getPassword(),
                    "cgn.pe.storage.azure.blob-endpoint",
                    "http://127.0.0.1:" + azurite.getMappedPort(10000) + "/devstoreaccount1", "spring.mail.host",
                    greenMailContainer.getHost(), "spring.mail.port",
                    String.valueOf(greenMailContainer.getFirstMappedPort()));
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            startContainers();
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            MapPropertySource testcontainers = new MapPropertySource("testcontainers",
                    (Map) createConnectionConfiguration());
            environment.getPropertySources().addFirst(testcontainers);
        }
    }

    @Getter
    @Setter
    protected class AgreementTestObject {
        private AgreementEntity agreementEntity;
        private List<DiscountEntity> discountEntityList;
        private List<DocumentEntity> documentEntityList;
        private ProfileEntity profileEntity;
    }

    @Autowired
    protected DiscountRepository discountRepository;

    @Autowired
    protected DiscountBucketCodeRepository discountBucketCodeRepository;

    @Autowired
    protected BucketCodeLoadRepository bucketCodeLoadRepository;

    @Autowired
    protected AgreementRepository agreementRepository;

    @Autowired
    protected ProfileRepository profileRepository;

    @Autowired
    protected AgreementUserRepository userRepository;

    @Autowired
    protected DocumentRepository documentRepository;

    @Autowired
    protected AgreementService agreementService;

    @Autowired
    protected ProfileService profileService;

    @Autowired
    protected DiscountService discountService;

    @Autowired
    protected BucketService bucketService;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected BackofficeAgreementService backofficeAgreementService;

    @Autowired
    protected ApprovedAgreementService approvedAgreementService;

    @Autowired
    protected TestReferentRepository testReferentRepository;

    @Autowired
    protected AddressRepository addressRepository;

    @Autowired
    protected DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository;

    @Autowired
    protected OfflineMerchantRepository offlineMerchantRepository;

    @Autowired
    protected OnlineMerchantRepository onlineMerchantRepository;

    @Autowired
    protected PublishedProductCategoryRepository publishedProductCategoryRepository;

    @AfterEach
    protected void cleanAll() throws InterruptedException {
        documentRepository.deleteAll();
        documentRepository.flush();
        discountBucketCodeRepository.deleteAll();
        discountBucketCodeRepository.flush();
        bucketCodeLoadRepository.deleteAll();
        bucketCodeLoadRepository.flush();
        discountRepository.deleteAll();
        discountRepository.flush();
        profileRepository.deleteAll();
        profileRepository.flush();
        agreementRepository.deleteAll();
        agreementRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
    }

    protected List<DocumentEntity> saveSampleDocuments(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = TestUtils.createSampleDocumentList(agreementEntity);
        return documentRepository.saveAll(documentList);
    }

    protected List<DocumentEntity> saveBackofficeSampleDocuments(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = TestUtils.createSampleBackofficeDocumentList(agreementEntity);
        return documentRepository.saveAll(documentList);
    }

    protected AgreementTestObject createPendingAgreement() {
        return createPendingAgreement(SalesChannelEnum.ONLINE, DiscountCodeTypeEnum.STATIC);
    }

    protected List<AgreementTestObject> createMultiplePendingAgreement(int numberToCreate) {
        List<AgreementTestObject> testObjectList = new ArrayList<>(numberToCreate);
        IntStream.range(0, numberToCreate).forEach(idx -> testObjectList
                .add(createPendingAgreement(SalesChannelEnum.ONLINE, DiscountCodeTypeEnum.STATIC, idx)));

        return testObjectList;
    }

    protected AgreementTestObject createPendingAgreement(SalesChannelEnum salesChannel,
                                                         DiscountCodeTypeEnum discountCodeType) {
        return createPendingAgreement(salesChannel, discountCodeType, 1);
    }

    protected AgreementTestObject createPendingAgreement(SalesChannelEnum salesChannel,
                                                         DiscountCodeTypeEnum discountCodeType, int idx) {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID + idx);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity, salesChannel,
                discountCodeType);
        profileEntity.setFullName(profileEntity.getFullName() + idx);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setName(discountEntity.getName() + idx);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        List<DocumentEntity> documentEntityList = saveSampleDocuments(agreementEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        return createAgreementTestObject(agreementEntity, profileEntity, discountEntity, documentEntityList);
    }

    protected List<AgreementTestObject> createMultipleApprovedAgreement(int numberToCreate) {
        List<AgreementTestObject> testObjectList = new ArrayList<>(numberToCreate);
        IntStream.range(0, numberToCreate).forEach(idx -> testObjectList.add(createApprovedAgreement(idx)));

        return testObjectList;
    }

    protected AgreementTestObject createApprovedAgreement() {
        return createApprovedAgreement(1);
    }

    protected AgreementTestObject createApprovedAgreement(int idx) {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID + idx);
        // creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity.setFullName(profileEntity.getFullName() + idx);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        // creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setName(discountEntity.getName() + idx);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        List<DocumentEntity> documentEntityList = saveSampleDocuments(agreementEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        documentEntityList.addAll(saveBackofficeSampleDocuments(agreementEntity));
        agreementEntity = backofficeAgreementService.approveAgreement(agreementEntity.getId());
        return createAgreementTestObject(agreementEntity, profileEntity, discountEntity, documentEntityList);
    }

    protected void setOperatorAuth() {
        TestUtils.setOperatorAuth();
    }

    protected void setAdminAuth() {
        TestUtils.setAdminAuth();
    }

    protected void setProfileSalesChannel(AgreementEntity agreementEntity, SalesChannelEnum salesChannel) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId()).orElseThrow();
        profileEntity.setSalesChannel(salesChannel);
        // to avoid LazyInitializationException
        profileEntity.setReferent(testReferentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileService.updateProfile(agreementEntity.getId(), profileEntity);
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    protected void setProfileDiscountType(AgreementEntity agreementEntity, DiscountCodeTypeEnum discountType) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId()).orElseThrow();
        profileEntity.setDiscountCodeType(discountType);
        // to avoid LazyInitializationException
        profileEntity.setReferent(testReferentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileService.updateProfile(agreementEntity.getId(), profileEntity);
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    protected void burnBucketCodesToLeaveLessThanThresholdCodes(BucketCodeExpiringThresholdEnum threshold, DiscountEntity discountEntity) {
        // use 100% - threshold codes
        int codeToUse = 10 - (int) Math.floor((float) 10 * threshold.getValue() / 100);
        log.info("Will use " + codeToUse + " codes.");
        discountBucketCodeRepository.findAllByDiscount(discountEntity).stream().limit(codeToUse).forEach(c -> {
            c.setIsUsed(true);
            discountBucketCodeRepository.save(c);
        });
    }

    private AgreementTestObject createAgreementTestObject(AgreementEntity agreementEntity, ProfileEntity profileEntity,
                                                          DiscountEntity discountEntity, List<DocumentEntity> documentEntityList) {
        AgreementTestObject testObject = new AgreementTestObject();
        testObject.setAgreementEntity(agreementEntity);
        testObject.setProfileEntity(profileEntity);
        testObject.setDiscountEntityList(Collections.singletonList(discountEntity));
        testObject.setDocumentEntityList(documentEntityList);
        return testObject;
    }

}
