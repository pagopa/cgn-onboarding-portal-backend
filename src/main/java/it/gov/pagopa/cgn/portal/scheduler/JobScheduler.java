package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service
public class JobScheduler {

    private final Scheduler scheduler;
    private final ConfigProperties configProperties;

    public JobScheduler(Scheduler scheduler, ConfigProperties configProperties) {
        this.scheduler = scheduler;
        this.configProperties = configProperties;
    }

    public void scheduleCheckExpiringDiscountsJob() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("check-expiring", "discounts");
        scheduleJob(jobKey, configProperties.getExpiringDiscountsJobCronExpression());
    }

    public void scheduleCheckAvailableDiscountBucketCodesJob() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("check-available-codes", "discounts");
        scheduleJob(jobKey, configProperties.getAvailableDiscountBucketCodesJobCronExpression());
    }

    public void scheduleSuspendDiscountsWithoutAvailableBucketCodesJob() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("suspend-discount-with-expired-bucket", "discounts");
        scheduleJob(jobKey, configProperties.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression());
    }

    private void scheduleJob(JobKey jobKey, String cronExpression) throws SchedulerException {
        for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
            scheduler.unscheduleJob(trigger.getKey());
        }

        JobDetail job = JobBuilder
                .newJob(CheckAvailableDiscountBucketCodesJob.class)
                .withIdentity(jobKey)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .inTimeZone(TimeZone.getTimeZone("Europe/Rome")))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
