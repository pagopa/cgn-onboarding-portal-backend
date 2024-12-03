package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.scheduler.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchedulerRunner
        implements ApplicationRunner {

    private final JobScheduler jobScheduler;

    @Autowired
    public SchedulerRunner(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }


    @Override
    public void run(ApplicationArguments args)
            throws Exception {
        jobScheduler.scheduleCheckExpiringDiscountsJob();
        jobScheduler.scheduleCheckAvailableDiscountBucketCodesJob();
        jobScheduler.scheduleSuspendDiscountsWithoutAvailableBucketCodesJob();
        jobScheduler.scheduleSendDiscountsToEycaJob();
    }
}
