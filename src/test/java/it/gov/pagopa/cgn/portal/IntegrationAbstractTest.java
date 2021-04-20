package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.AgreementUserRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;
import java.util.stream.Stream;
@ContextConfiguration(initializers = IntegrationAbstractTest.Initializer.class)
public class IntegrationAbstractTest {


    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:11-alpine")
                .withDatabaseName("integration-tests-db")
                .withUsername("admin")
                .withPassword("admin");

        private static void startContainers() {
            Startables.deepStart(Stream.of(postgres)).join();
            // we can add further containers here like rabbitmq or other database
        }

        private static Map<String, String> createConnectionConfiguration() {
            return Map.of(
                    "spring.datasource.url", postgres.getJdbcUrl(),
                    "spring.datasource.username", postgres.getUsername(),
                    "spring.datasource.password", postgres.getPassword()
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

    @AfterEach
    protected void cleanAll() {
        discountRepository.deleteAll();
        profileRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
    }
}
