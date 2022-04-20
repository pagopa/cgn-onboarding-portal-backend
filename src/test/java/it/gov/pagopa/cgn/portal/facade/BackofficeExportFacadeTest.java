package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class BackofficeExportFacadeTest extends IntegrationAbstractTest {

    private AgreementEntity agreementEntity;

    private ProfileEntity profileEntity;

    @BeforeEach
    void init() {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    @Test
    void ExportAgreements_DRAFT_NO_DISCOUNTS_OK() {
        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void ExportAgreements_DRAFT_WITH_DICOUNT_OK() {
        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity1.setName("Discount 1");
        discountService.createDiscount(agreementEntity.getId(), discountEntity1).getDiscountEntity();

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity2.setName("Discount 2");
        discountService.createDiscount(agreementEntity.getId(), discountEntity2).getDiscountEntity();

        ResponseEntity<Resource> response = backofficeExportFacade.exportAgreements();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

}
