package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementInactivationServiceTest
        extends IntegrationAbstractTest {

    @Autowired
    private AgreementInactivationService agreementInactivationService;

    private LocalDate cutoff;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
        cutoff = LocalDate.now().minusMonths(6);
    }

    @Test
    void InactivateStaleAgreements_ApprovedWithoutPublishedDiscountSinceCutoff_Inactivated() {
        AgreementEntity agreement = createApprovedAgreement(1, false).getAgreementEntity();
        agreement.setStartDate(cutoff);
        agreement = agreementRepository.save(agreement);

        int inactivatedAgreements = agreementInactivationService.inactivateStaleAgreements(cutoff);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(1, inactivatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.INACTIVE, updatedAgreement.getState());
        Assertions.assertEquals(LocalDate.now(), updatedAgreement.getInformationLastUpdateDate());
        assertInactiveAuditWasWritten(agreement.getId());
    }

    @Test
    void InactivateStaleAgreements_ApprovedWithoutPublishedDiscountAfterCutoff_NotInactivated() {
        AgreementEntity agreement = createApprovedAgreement(1, false).getAgreementEntity();
        agreement.setStartDate(cutoff.plusDays(1));
        agreement = agreementRepository.save(agreement);

        int inactivatedAgreements = agreementInactivationService.inactivateStaleAgreements(cutoff);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, inactivatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.APPROVED, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_ActiveWithAllPublishedDiscountsExpiredSinceCutoff_Inactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setEndDate(cutoff);
            discountRepository.save(discount);
        });

        int inactivatedAgreements = agreementInactivationService.inactivateStaleAgreements(cutoff);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(1, inactivatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.INACTIVE, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_ActiveWithPublishedDiscountAfterCutoff_NotInactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        DiscountEntity firstDiscount = testObject.getDiscountEntityList().get(0);
        DiscountEntity secondDiscount = testObject.getDiscountEntityList().get(1);
        firstDiscount.setEndDate(cutoff);
        secondDiscount.setEndDate(cutoff.plusDays(1));
        discountRepository.save(firstDiscount);
        discountRepository.save(secondDiscount);

        int inactivatedAgreements = agreementInactivationService.inactivateStaleAgreements(cutoff);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, inactivatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.ACTIVE, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_ActiveWithoutPublishedDiscounts_NotInactivated() {
        AgreementEntity agreement = createApprovedAgreement(1, false).getAgreementEntity();
        agreement.setState(AgreementStateEnum.ACTIVE);
        agreement.setFirstDiscountPublishingDate(cutoff.minusDays(1));
        agreement = agreementRepository.save(agreement);

        int inactivatedAgreements = agreementInactivationService.inactivateStaleAgreements(cutoff);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, inactivatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.ACTIVE, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_TerminationStates_NotInactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setEndDate(cutoff);
            discountRepository.save(discount);
        });
        agreement.setState(AgreementStateEnum.TERMINATION_REMINDER_SENT);
        agreement = agreementRepository.save(agreement);

        int inactivatedAgreements = agreementInactivationService.inactivateStaleAgreements(cutoff);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, inactivatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.TERMINATION_REMINDER_SENT, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_RefreshesMerchantMaterializedViews() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        onlineMerchantRepository.refreshView();
        Assertions.assertFalse(onlineMerchantRepository.findAll().isEmpty());

        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setEndDate(cutoff);
            discountRepository.save(discount);
        });

        agreementInactivationService.inactivateStaleAgreements(cutoff);

        Assertions.assertTrue(onlineMerchantRepository.findAll().stream()
                                                      .noneMatch(merchant -> agreement.getId().equals(merchant.getId())));
    }

    private void assertInactiveAuditWasWritten(String agreementId) {
        boolean inactiveAuditExists = changeAuditRepository.findAll()
                                                           .stream()
                                                           .anyMatch(audit -> agreementId.equals(audit.getSubjectId()) &&
                                                                              ChangeAuditSubjectTypeEnum.AGREEMENT.equals(
                                                                                      audit.getSubjectType()) &&
                                                                              ChangeAuditOperationTypeEnum.UPDATE.equals(
                                                                                      audit.getOperationType()) &&
                                                                              AgreementStateEnum.INACTIVE.name()
                                                                                                .equals(audit.getValue()
                                                                                                             .get("state")));
        Assertions.assertTrue(inactiveAuditExists);
    }
}
