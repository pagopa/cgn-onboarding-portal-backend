package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.util.CsvUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("dev")
class BackofficeExportFacadeTest extends IntegrationAbstractTest {

    private AgreementEntity agreementEntity;

    @BeforeEach
    void init() {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE,"ExampleOrganizationName");
    }

    private void createProfile() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
    }

    @Test
    void ExportAgreements_DRAFT_NO_PROFILE_OK() throws IOException {
        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }

    @Test
    void ExportAgreements_DRAFT_WITH_PROFILE_OK() throws IOException {
        createProfile();
        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }

    @Test
    void ExportAgreements_DRAFT_WITH_PROFILE_NO_DISCOUNTS_OK() throws IOException {
        createProfile();
        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }

    @Test
    void ExportAgreements_DRAFT_WITH_PROFILE_WITH_DISCOUNTS_OK() throws IOException {
        createProfile();
        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity1.setName("Discount 1");
        discountService.createDiscount(agreementEntity.getId(), discountEntity1);

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity2.setName("Discount 2");
        discountService.createDiscount(agreementEntity.getId(), discountEntity2);

        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(3, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }

    @Test
    void ExportAgreements_DRAFT_WITH_PROFILE_WITHOUT_DISCOUNTS_OK() throws IOException {
        createProfile();
        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity1.setName("Discount 1");
        discountService.createDiscount(agreementEntity.getId(), discountEntity1);

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity2.setName("Discount 2");
        discountService.createDiscount(agreementEntity.getId(), discountEntity2);

        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(3, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }


    @Test
    void ExportAgreements_DRAFT_WITH_PROFILE_WITH_PUBLISHED_EXPIRED_DISCOUNTS() throws IOException {
        createProfile();
        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity1.setName("Discount 1");
        discountEntity1.setState(DiscountStateEnum.PUBLISHED);
        discountEntity1.setStartDate(LocalDate.now().minusDays(10));
        discountEntity1.setEndDate(LocalDate.now().minusDays(2));
        discountService.createDiscount(agreementEntity.getId(), discountEntity1);

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity2.setName("Discount 2");
        discountService.createDiscount(agreementEntity.getId(), discountEntity2);

        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(3, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }


    @Test
    void ExportEycaDiscounts_OK() throws IOException {
        createProfile();

        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity1.setName("Discount 1");
        discountEntity1.setVisibleOnEyca(true);
        discountEntity1.setState(DiscountStateEnum.PUBLISHED);
        discountEntity1.setEndDate(LocalDate.now().plusDays(10));
        discountService.createDiscount(agreementEntity.getId(), discountEntity1);

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity2.setName("Discount 2");
        discountEntity2.setVisibleOnEyca(true);
        discountEntity2.setState(DiscountStateEnum.PUBLISHED);
        discountEntity2.setEndDate(LocalDate.now().plusDays(10));
        discountService.createDiscount(agreementEntity.getId(), discountEntity2);

        ResponseEntity<Resource> response = backofficeExportFacade.exportEycaDiscounts();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(3, CsvUtils.countCsvLines(response.getBody().getInputStream()));
    }



}
