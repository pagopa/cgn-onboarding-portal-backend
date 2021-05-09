package it.gov.pagopa.cgn.portal.service.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles({"dev"})
class BackofficeApprovedAgreementServiceTest extends IntegrationAbstractTest {

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
    }

    @Test
    void GetApprovedAgreements_GetAgreementsWithoutFilter_AgreementFound() {
        AgreementEntity agreementEntity = createApprovedAgreement().getAgreementEntity();

        BackofficeFilter filter = BackofficeFilter.builder().build();
        Page<AgreementEntity> page = backofficeAgreementService.getApprovedAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        AgreementEntity responseAgreement = page.getContent().get(0);
        Assertions.assertEquals(agreementEntity.getId(), responseAgreement.getId());
        Assertions.assertNotNull(agreementEntity.getInformationLastUpdateDate());
        Assertions.assertNotNull(agreementEntity.getStartDate());
    }


    @Test
    void GetApprovedAgreements_GetAgreementsWithProfileDateFilter_AgreementFound() {
        AgreementEntity agreementEntity = createApprovedAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                .dateFrom(LocalDate.now().minusDays(10))
                .dateTo(LocalDate.now().plusDays(10))
                .build();
        Page<AgreementEntity> page = backofficeAgreementService.getApprovedAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        AgreementEntity responseAgreement = page.getContent().get(0);
        Assertions.assertEquals(agreementEntity.getId(), responseAgreement.getId());
        Assertions.assertNotNull(agreementEntity.getInformationLastUpdateDate());
        Assertions.assertNotNull(agreementEntity.getStartDate());
    }

    @Test
    void GetApprovedAgreements_GetAgreementsWithProfileDateFilter_AgreementNotFound() {
        createApprovedAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                .dateFrom(LocalDate.now().plusDays(2))
                .dateTo(LocalDate.now().plusDays(10))
                .build();
        Page<AgreementEntity> page = backofficeAgreementService.getApprovedAgreements(filter);
        Assertions.assertEquals(0L, page.getTotalElements());
        Assertions.assertEquals(0, page.getTotalPages());
    }
    @Test
    void GetApprovedAgreementDetail_GetApprovedAgreementDetail_AgreementDetailFound() {
        AgreementTestObject agreementTestObject = createApprovedAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        DiscountEntity discountEntity = agreementTestObject.getDiscountEntityList().get(0);
        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());

        AgreementEntity approvedAgreement = agreementService.getApprovedAgreement(agreementEntity.getId());
        Assertions.assertNotNull(approvedAgreement);
        Assertions.assertEquals(agreementEntity.getId(), approvedAgreement.getId());
        Assertions.assertFalse(CollectionUtils.isEmpty(approvedAgreement.getDiscountList()));
        Assertions.assertFalse(CollectionUtils.isEmpty(approvedAgreement.getDocumentList()));
    }

    @Test
    void GetApprovedAgreementDetail_GetApprovedAgreementDetailWithoutPublicDiscount_AgreementDetailFound() {
        AgreementEntity agreementEntity = createApprovedAgreement().getAgreementEntity();
        AgreementEntity approvedAgreement = agreementService.getApprovedAgreement(agreementEntity.getId());
        Assertions.assertNotNull(approvedAgreement);
        Assertions.assertEquals(agreementEntity.getId(), approvedAgreement.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(approvedAgreement.getDiscountList()));
        Assertions.assertFalse(CollectionUtils.isEmpty(approvedAgreement.getDocumentList()));
    }

}
