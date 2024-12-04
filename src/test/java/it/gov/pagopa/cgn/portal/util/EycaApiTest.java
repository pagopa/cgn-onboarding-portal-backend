package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.scheduler.JobScheduler;
import it.gov.pagopa.cgn.portal.service.EycaExportService;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ListApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ListDataExportEyca;
import org.apache.http.util.Asserts;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@SpringBootTest
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class, HibernateJpaAutoConfiguration.class, QuartzAutoConfiguration.class})
@MockBeans(value = {
        @MockBean(AddressRepository.class),
        @MockBean(AgreementRepository.class),
        @MockBean(AgreementUserRepository.class),
        @MockBean(ApprovedAgreementRepository.class),
        @MockBean(BucketCodeLoadRepository.class),
        @MockBean(DiscountBucketCodeRepository.class),
        @MockBean(DiscountBucketCodeSummaryRepository.class),
        @MockBean(DiscountRepository.class),
        @MockBean(DocumentRepository.class),
        @MockBean(EycaDataExportRepository.class),
        @MockBean(NotificationRepository.class),
        @MockBean(OfflineMerchantRepository.class),
        @MockBean(OnlineMerchantRepository.class),
        @MockBean(ProfileRepository.class),
        @MockBean(PublishedProductCategoryRepository.class),
        @MockBean(SecondaryReferentRepository.class),
        @MockBean (JobScheduler.class),
        @MockBean (TestReferentRepository.class)
})
@RunWith(SpringRunner.class)
public class EycaApiTest {

    @Autowired
    @Qualifier("restTemplateForTest")
    private RestTemplate restTemplate;

    @Autowired
    private EycaExportService eycaExportService;

    @Qualifier("ApiClientForTest")
    @Autowired
    ApiClient apiClient;

    @TestConfiguration
    static class TestApiConfiguration {

        @Bean(name="restTemplateForTest")
        public RestTemplate getRestTemplateForTest() {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory());
            return restTemplate;
        }
        @Bean(name = "ApiClientForTest")
        @Primary
        public ApiClient getApiClientForTest () {
            return new ApiClient(getRestTemplateForTest());
        }
    }

    @Test
    public void testRestTemplate() {
        Assertions.assertNotNull(restTemplate.getUriTemplateHandler(), "UriTemplateHandler should not be null");
    }
    @Test
    @Ignore
    public void testCallListApiEyca_ok () {
        final ListDataExportEyca listDataExportEyca = new ListDataExportEyca();
        listDataExportEyca.setPage(1);
        listDataExportEyca.setRows(1000);

        eycaExportService.authenticateOnEyca();
        ListApiResponseEyca lare = eycaExportService.listDiscounts(listDataExportEyca, "json");
        Assert.assertNotNull(lare.getApiResponse().getData().getDiscount());
    }
}
