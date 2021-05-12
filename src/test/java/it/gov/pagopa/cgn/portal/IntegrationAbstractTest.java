package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.BackofficeAgreementService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.SalesChannel;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ContextConfiguration(initializers = IntegrationAbstractTest.Initializer.class)
public class IntegrationAbstractTest {

    protected String getAzureConnectionString() {
        return "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;" +
                "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
                "BlobEndpoint=http://127.0.0.1:" + Initializer.azurite.getMappedPort(10000) + "/devstoreaccount1;";
    }


    protected static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:11-alpine")
                .withDatabaseName("integration-tests-db")
                .withUsername("admin")
                .withPassword("admin");

        public static GenericContainer<?> azurite =
                new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.11.0"))
                        .withExposedPorts(10000);

        public static GenericContainer<?> greenMailContainer =
                new GenericContainer<>(DockerImageName.parse("greenmail/standalone:1.6.3"))
                        .withExposedPorts(3025);


        private static void startContainers() {
            Startables.deepStart(Stream.of(postgres, azurite, greenMailContainer)).join();
        }

        private static Map<String, String> createConnectionConfiguration() {
            return Map.of(
                    "spring.datasource.url", postgres.getJdbcUrl(),
                    "spring.datasource.username", postgres.getUsername(),
                    "spring.datasource.password", postgres.getPassword(),
                    "cgn.pe.storage.azure.blob-endpoint", "http://127.0.0.1:" + azurite.getMappedPort(10000) + "/devstoreaccount1",
                    "spring.mail.host", greenMailContainer.getHost(),
                    "spring.mail.port", String.valueOf(greenMailContainer.getFirstMappedPort())
            );
        }


        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            startContainers();
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            MapPropertySource testcontainers = new MapPropertySource(
                    "testcontainers",
                    (Map) createConnectionConfiguration()
            );
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
    protected BackofficeAgreementService backofficeAgreementService;

    @AfterEach
    protected void cleanAll() {
        documentRepository.deleteAll();
        discountRepository.deleteAll();
        profileRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
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

    protected AgreementTestObject createPendingAgreement(SalesChannelEnum salesChannel, DiscountCodeTypeEnum discountCodeType) {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity, salesChannel, discountCodeType);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        List<DocumentEntity> documentEntityList = saveSampleDocuments(agreementEntity);
        agreementEntity= agreementService.requestApproval(agreementEntity.getId());
        return createAgreementTestObject(agreementEntity, profileEntity, discountEntity, documentEntityList);
    }

    protected AgreementTestObject createApprovedAgreement() {
        // creating agreement (and user)
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        //creating profile
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());
        //creating discount
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        List<DocumentEntity> documentEntityList = saveSampleDocuments(agreementEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        documentEntityList.addAll(saveBackofficeSampleDocuments(agreementEntity));
        agreementEntity = backofficeAgreementService.approveAgreement(agreementEntity.getId());
        return createAgreementTestObject(agreementEntity, profileEntity, discountEntity, documentEntityList);
    }

    protected void setOperatorAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new JwtOperatorUser(TestUtils.FAKE_ID, TestUtils.FAKE_ID, "merchant_name"))
        );
    }

    protected void setAdminAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new JwtAdminUser(TestUtils.FAKE_ID, "admin_name"))
        );
    }

    private AgreementTestObject createAgreementTestObject(AgreementEntity agreementEntity, ProfileEntity profileEntity,
                                                          DiscountEntity discountEntity,
                                                          List<DocumentEntity> documentEntityList) {
        AgreementTestObject testObject = new AgreementTestObject();
        testObject.setAgreementEntity(agreementEntity);
        testObject.setProfileEntity(profileEntity);
        testObject.setDiscountEntityList(Collections.singletonList(discountEntity));
        testObject.setDocumentEntityList(documentEntityList);
        return testObject;
    }



}
