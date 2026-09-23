package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@SpringBootTest
@ActiveProfiles({"dev"})
class InactivateAgreementsJobTest
        extends IntegrationAbstractTest {

    @Autowired
    private InactivateAgreementsJob job;

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private Scheduler quartzScheduler;

    private LocalDate currentDate;
    private LocalDate expiredAgreementCutoff;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
        currentDate = LocalDate.now(ZoneOffset.UTC);
        expiredAgreementCutoff = currentDate.minusMonths(6);
    }

    @Test
    void Execute_ExecuteJobDoesNotUpdateApprovedAgreementWithoutPublishedDiscount_Ok() {
        AgreementEntity agreement = createApprovedAgreement(1, false).getAgreementEntity();
        agreement.setStartDate(currentDate.minusYears(1));
        agreementRepository.save(agreement);

        job.execute(null);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(AgreementStateEnum.APPROVED, updatedAgreement.getState());
    }

    @Test
    void Execute_ExecuteJobExpiresActiveAgreementWithoutValidDiscount_Ok() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setState(DiscountStateEnum.PUBLISHED);
            discount.setStartDate(currentDate.minusDays(2));
            discount.setEndDate(currentDate.minusDays(1));
            discountRepository.save(discount);
        });

        job.execute(null);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(AgreementStateEnum.EXPIRED, updatedAgreement.getState());
    }

    @Test
    void Execute_ExecuteJobExpiresActiveAgreementWithoutPublishedDiscount_Ok() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setState(DiscountStateEnum.DRAFT);
            discountRepository.save(discount);
        });

        job.execute(null);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(AgreementStateEnum.EXPIRED, updatedAgreement.getState());
    }

    @Test
    void Execute_ExecuteJobInactivatesExpiredAgreementWithoutPublishedDiscount_Ok() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setState(DiscountStateEnum.DRAFT);
            discountRepository.save(discount);
        });
        agreement.setState(AgreementStateEnum.EXPIRED);
        agreementRepository.save(agreement);
        setCurrentStateAuditInsertTime(agreement.getId(),
                                       AgreementStateEnum.EXPIRED,
                                       expiredAgreementCutoff.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));

        job.execute(null);

        AgreementEntity updatedAgreement = agreementRepository.findById(agreement.getId()).orElseThrow();
        Assertions.assertEquals(AgreementStateEnum.INACTIVE, updatedAgreement.getState());
    }

    @Test
    void Scheduler_ScheduleInactivateAgreementsJob_JobScheduled()
            throws SchedulerException {
        jobScheduler.scheduleInactivateAgreementsJob();
        List<? extends Trigger> triggersOfJob = quartzScheduler.getTriggersOfJob(JobKey.jobKey("inactivate-agreements",
                                                                                               "agreements"));
        Assertions.assertFalse(triggersOfJob.isEmpty());
        Assertions.assertNotNull(triggersOfJob.get(0).getNextFireTime());
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
