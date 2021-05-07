package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

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

    @AfterEach
    protected void cleanAll() {
        documentRepository.deleteAll();
        discountRepository.deleteAll();
        profileRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected void saveSampleDocuments(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = TestUtils.createSampleDocumentList(agreementEntity);
        documentRepository.saveAll(documentList);
    }

    protected void saveBackofficeSampleDocuments(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = TestUtils.createSampleBackofficeDocumentList(agreementEntity);
        documentRepository.saveAll(documentList);
    }

}
