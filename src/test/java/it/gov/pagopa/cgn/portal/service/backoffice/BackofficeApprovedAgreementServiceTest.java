package it.gov.pagopa.cgn.portal.service.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.stream.Collectors;

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
        Page<ApprovedAgreementEntity> page = approvedAgreementService.getApprovedAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        ApprovedAgreementEntity responseAgreement = page.getContent().get(0);
        Assertions.assertEquals(agreementEntity.getId(), responseAgreement.getId());
        Assertions.assertNotNull(agreementEntity.getInformationLastUpdateDate());
        Assertions.assertNotNull(agreementEntity.getStartDate());
        Assertions.assertEquals(agreementEntity.getId(), responseAgreement.getId());
        Assertions.assertEquals(agreementEntity.getStartDate(), responseAgreement.getStartDate());
        Assertions.assertEquals(agreementEntity.getInformationLastUpdateDate(),
                                responseAgreement.getInformationLastUpdateDate());
        Assertions.assertEquals(agreementEntity.getProfile().getFullName(), responseAgreement.getFullName());
        Assertions.assertEquals(0, responseAgreement.getPublishedDiscounts());
    }

    @Test
    void GetApprovedAgreements_GetAgreementsWithProfileDateFilter_AgreementFound() {
        AgreementEntity agreementEntity = createApprovedAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .dateFrom(LocalDate.now().minusDays(10))
                                                  .dateTo(LocalDate.now().plusDays(10))
                                                  .build();
        Page<ApprovedAgreementEntity> page = approvedAgreementService.getApprovedAgreements(filter);
        Assertions.assertEquals(1L, page.getTotalElements());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertNotNull(page.getContent());
        Assertions.assertFalse(page.getContent().isEmpty());
        ApprovedAgreementEntity responseAgreement = page.getContent().get(0);
        Assertions.assertEquals(agreementEntity.getId(), responseAgreement.getId());
        Assertions.assertNotNull(agreementEntity.getInformationLastUpdateDate());
        Assertions.assertNotNull(agreementEntity.getStartDate());
        Assertions.assertEquals(agreementEntity.getId(), responseAgreement.getId());
        Assertions.assertEquals(agreementEntity.getStartDate(), responseAgreement.getStartDate());
        Assertions.assertEquals(agreementEntity.getInformationLastUpdateDate(),
                                responseAgreement.getInformationLastUpdateDate());
        Assertions.assertEquals(agreementEntity.getProfile().getFullName(), responseAgreement.getFullName());
        Assertions.assertEquals(0, responseAgreement.getPublishedDiscounts());
    }

    @Test
    void GetApprovedAgreements_GetAgreementsWithProfileDateFilter_AgreementNotFound() {
        createApprovedAgreement().getAgreementEntity();
        BackofficeFilter filter = BackofficeFilter.builder()
                                                  .dateFrom(LocalDate.now().plusDays(2))
                                                  .dateTo(LocalDate.now().plusDays(10))
                                                  .build();
        Page<ApprovedAgreementEntity> page = approvedAgreementService.getApprovedAgreements(filter);
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
    void GetApprovedAgreementDetail_GetApprovedAgreementDetailWithPublishedAndSuspendedDiscounts_AgreementDetailFound() {
        AgreementTestObject agreementTestObject = createApprovedAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        DiscountEntity discountEntity = agreementTestObject.getDiscountEntityList().get(0);
        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity2 = discountService.createDiscount(agreementEntity.getId(), discountEntity2).getDiscountEntity();
        discountService.publishDiscount(agreementEntity.getId(), discountEntity2.getId());
        discountService.suspendDiscount(agreementEntity.getId(), discountEntity2.getId(), "Bad discount");

        AgreementEntity approvedAgreement = agreementService.getApprovedAgreement(agreementEntity.getId());
        Assertions.assertNotNull(approvedAgreement);
        Assertions.assertEquals(agreementEntity.getId(), approvedAgreement.getId());
        Assertions.assertEquals(2, approvedAgreement.getDiscountList().size());
        Assertions.assertFalse(CollectionUtils.isEmpty(approvedAgreement.getDiscountList()
                                                                        .stream()
                                                                        .filter(d -> d.getState() ==
                                                                                     DiscountStateEnum.PUBLISHED)
                                                                        .collect(Collectors.toList())));
        Assertions.assertFalse(CollectionUtils.isEmpty(approvedAgreement.getDiscountList()
                                                                        .stream()
                                                                        .filter(d -> d.getState() ==
                                                                                     DiscountStateEnum.SUSPENDED)
                                                                        .collect(Collectors.toList())));

    }

    @Test
    void GetApprovedAgreementDetail_GetApprovedAgreementDetailWithoutPublishedDiscount_AgreementDetailFound() {
        AgreementEntity agreementEntity = createApprovedAgreement().getAgreementEntity();
        AgreementEntity approvedAgreement = agreementService.getApprovedAgreement(agreementEntity.getId());
        Assertions.assertNotNull(approvedAgreement);
        Assertions.assertEquals(agreementEntity.getId(), approvedAgreement.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(approvedAgreement.getDiscountList()));
        Assertions.assertFalse(CollectionUtils.isEmpty(approvedAgreement.getDocumentList()));
    }

}
