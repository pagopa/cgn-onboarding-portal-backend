package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementInactivationServiceTest
        extends IntegrationAbstractTest {

    private static final int EXPIRED_AGREEMENT_STALE_MONTHS = 6;

    @Autowired
    private AgreementInactivationService agreementInactivationService;

    private LocalDate currentDate;
    private LocalDate expiredAgreementCutoff;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
        currentDate = LocalDate.now(ZoneOffset.UTC);
        expiredAgreementCutoff = currentDate.minusMonths(EXPIRED_AGREEMENT_STALE_MONTHS);
    }

    @Test
    void InactivateStaleAgreements_ApprovedWithoutPublishedDiscountEvenIfStale_NotUpdated() {
        AgreementEntity agreement = createApprovedAgreement(1, false).getAgreementEntity();
        agreement.setStartDate(currentDate.minusYears(1));
        agreement = agreementRepository.save(agreement);

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.APPROVED, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_ApprovedWithoutPublishedDiscountRecently_NotUpdated() {
        AgreementEntity agreement = createApprovedAgreement(1, false).getAgreementEntity();
        agreement.setStartDate(currentDate.minusDays(1));
        agreement = agreementRepository.save(agreement);

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.APPROVED, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_ActiveWithAllPublishedDiscountsExpired_Expired() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setStartDate(currentDate.minusDays(2));
            discount.setEndDate(currentDate.minusDays(1));
            discountRepository.save(discount);
        });

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(1, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.EXPIRED, updatedAgreement.getState());
        assertAgreementStateAuditWasWritten(agreement.getId(), AgreementStateEnum.EXPIRED);
    }

    @Test
    void InactivateStaleAgreements_ExpiredWithAllPublishedDiscountsExpiredSinceCutoff_Inactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setStartDate(expiredAgreementCutoff.minusDays(1));
            discount.setEndDate(expiredAgreementCutoff);
            discountRepository.save(discount);
        });
        agreement.setState(AgreementStateEnum.EXPIRED);
        agreement = agreementRepository.save(agreement);
        setCurrentStateAuditInsertTime(agreement.getId(),
                           AgreementStateEnum.EXPIRED,
                           expiredAgreementCutoff.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(1, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.INACTIVE, updatedAgreement.getState());
        assertAgreementStateAuditWasWritten(agreement.getId(), AgreementStateEnum.INACTIVE);
    }

    @Test
    void InactivateStaleAgreements_ActiveWithPublishedDiscountAfterCutoff_NotInactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        DiscountEntity firstDiscount = testObject.getDiscountEntityList().get(0);
        DiscountEntity secondDiscount = testObject.getDiscountEntityList().get(1);
        firstDiscount.setStartDate(currentDate.minusDays(2));
        firstDiscount.setEndDate(currentDate.minusDays(1));
        secondDiscount.setStartDate(currentDate.minusDays(1));
        secondDiscount.setEndDate(currentDate.plusDays(1));
        discountRepository.save(firstDiscount);
        discountRepository.save(secondDiscount);

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.ACTIVE, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_ActiveWithoutPublishedDiscounts_Expired() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setState(DiscountStateEnum.DRAFT);
            discountRepository.save(discount);
        });

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(1, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.EXPIRED, updatedAgreement.getState());
        assertAgreementStateAuditWasWritten(agreement.getId(), AgreementStateEnum.EXPIRED);
    }

    @Test
    void InactivateStaleAgreements_TerminationStates_NotInactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setStartDate(currentDate.minusDays(2));
            discount.setEndDate(currentDate.minusDays(1));
            discountRepository.save(discount);
        });
        agreement.setState(AgreementStateEnum.TERMINATION_REMINDER_SENT);
        agreement = agreementRepository.save(agreement);

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(0, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.TERMINATION_REMINDER_SENT, updatedAgreement.getState());
    }

    @Test
    void InactivateStaleAgreements_RefreshesMerchantMaterializedViews() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        onlineMerchantRepository.refreshView();
        Assertions.assertFalse(onlineMerchantRepository.findAll().isEmpty());

        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setStartDate(currentDate.minusDays(2));
            discount.setEndDate(currentDate.minusDays(1));
            discountRepository.save(discount);
        });

        agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        Assertions.assertTrue(onlineMerchantRepository.findAll().stream()
                                                      .noneMatch(merchant -> agreement.getId().equals(merchant.getId())));
    }

    @Test
    void InactivateStaleAgreements_ExpiredWithoutPublishedDiscountsSinceCutoff_Inactivated() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setState(DiscountStateEnum.DRAFT);
            discountRepository.save(discount);
        });
        agreement.setState(AgreementStateEnum.EXPIRED);
        agreement = agreementRepository.save(agreement);
        setCurrentStateAuditInsertTime(agreement.getId(),
                                       AgreementStateEnum.EXPIRED,
                                       expiredAgreementCutoff.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));

        int updatedAgreements = agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            EXPIRED_AGREEMENT_STALE_MONTHS);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(1, updatedAgreements);
        Assertions.assertEquals(AgreementStateEnum.INACTIVE, updatedAgreement.getState());
        assertAgreementStateAuditWasWritten(agreement.getId(), AgreementStateEnum.INACTIVE);
    }

    private void assertAgreementStateAuditWasWritten(String agreementId, AgreementStateEnum expectedState) {
        boolean stateAuditExists = changeAuditRepository.findAll()
                                                        .stream()
                                                        .anyMatch(audit -> agreementId.equals(audit.getSubjectId()) &&
                                                                           ChangeAuditSubjectTypeEnum.AGREEMENT.equals(
                                                                                   audit.getSubjectType()) &&
                                                                           ChangeAuditOperationTypeEnum.UPDATE.equals(
                                                                                   audit.getOperationType()) &&
                                                                           expectedState.name().equals(audit.getValue()
                                                                                                            .get("state")));
        Assertions.assertTrue(stateAuditExists);
    }

    private void setCurrentStateAuditInsertTime(String agreementId,
                                                AgreementStateEnum state,
                                                OffsetDateTime insertTime) {
        ChangeAuditEntity audit = changeAuditRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                                                       .stream()
                                                       .filter(changeAudit -> agreementId.equals(changeAudit.getSubjectId()))
                                                       .filter(changeAudit -> state.name().equals(changeAudit.getValue().get("state")))
                                                       .findFirst()
                                                       .orElseThrow();
        audit.setInsertTime(insertTime);
        changeAuditRepository.saveAndFlush(audit);
    }
}
