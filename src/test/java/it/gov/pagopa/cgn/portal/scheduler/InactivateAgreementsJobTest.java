package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
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

    private LocalDate cutoff;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
        cutoff = LocalDate.now().minusMonths(6);
    }

    @Test
    void Execute_ExecuteJobInactivatesStaleAgreement_Ok() {
        AgreementTestObject testObject = createApprovedAgreement(1, true);
        AgreementEntity agreement = testObject.getAgreementEntity();
        testObject.getDiscountEntityList().forEach(discount -> {
            discount.setState(DiscountStateEnum.PUBLISHED);
            discount.setEndDate(cutoff);
            discountRepository.save(discount);
        });

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
}
