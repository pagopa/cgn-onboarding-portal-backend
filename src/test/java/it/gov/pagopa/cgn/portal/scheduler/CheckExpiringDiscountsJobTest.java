package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
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
class CheckExpiringDiscountsJobTest
        extends IntegrationAbstractTest {

    @Autowired
    private CheckExpiringDiscountsJob job;

    @Autowired
    private Scheduler quartzScheduler;

    @Autowired
    private JobScheduler jobScheduler;

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
    }

    @Test
    void Execute_ExecuteJobUpdateExpiringDiscount_Ok() {
        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        DiscountEntity discountEntity = testObject.getDiscountEntityList().get(0);

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        discountEntity.setEndDate(LocalDate.now().plusDays(3));
        discountRepository.save(discountEntity);

        job.execute(null);
        discountEntity = discountRepository.findById(discountEntity.getId()).orElseThrow();
        Assertions.assertNotNull(discountEntity.getExpirationWarningSentDateTime());
        Assertions.assertEquals(LocalDate.now(), discountEntity.getExpirationWarningSentDateTime().toLocalDate());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, discountEntity.getState());
    }

    @Test
    void Execute_ExecuteJobWithoutPublicDiscount_Ok() {
        AgreementTestObject testObject = createApprovedAgreement();
        DiscountEntity discountEntity = testObject.getDiscountEntityList().get(0);
        Assertions.assertDoesNotThrow(() -> job.execute(null));
        discountEntity = discountRepository.findById(discountEntity.getId()).orElseThrow();
        Assertions.assertNull(discountEntity.getExpirationWarningSentDateTime());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, discountEntity.getState());
    }

    @Test
    void Scheduler_ScheduleCheckExpiringDiscountsJob_JobScheduled()
            throws SchedulerException {
        jobScheduler.scheduleCheckExpiringDiscountsJob();
        List<? extends Trigger> triggersOfJob = quartzScheduler.getTriggersOfJob(JobKey.jobKey("check-expiring",
                                                                                               "discounts"));
        Assertions.assertFalse(triggersOfJob.isEmpty());
        Assertions.assertNotNull(triggersOfJob.get(0).getNextFireTime());
    }
}
