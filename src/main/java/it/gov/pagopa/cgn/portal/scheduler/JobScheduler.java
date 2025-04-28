package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service
@Slf4j
public class JobScheduler {

    private static final String DISCOUNTS_JOB_GROUP = "discounts";
    private final Scheduler scheduler;
    private final ConfigProperties configProperties;

    public JobScheduler(Scheduler scheduler, ConfigProperties configProperties) {
        this.scheduler = scheduler;
        this.configProperties = configProperties;
    }

    public void scheduleCheckExpiringDiscountsJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("check-expiring", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey, configProperties.getExpiringDiscountsJobCronExpression(), CheckExpiringDiscountsJob.class);
    }

    public void scheduleCheckAvailableDiscountBucketCodesJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("check-available-codes", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    configProperties.getAvailableDiscountBucketCodesJobCronExpression(),
                    CheckAvailableDiscountBucketCodesJob.class);
    }

    public void scheduleSuspendDiscountsWithoutAvailableBucketCodesJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("suspend-discount-with-expired-bucket", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    configProperties.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression(),
                    SuspendDiscountsWithoutAvailableBucketCodesJob.class);
    }

    public void scheduleSendDiscountsToEycaJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("send-discount-to-eyca", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey, configProperties.getSendDiscountsToEycaJobCronExpression(), SendDiscountsToEycaJob.class);
    }


    private void scheduleJob(JobKey jobKey, String cronExpression, Class<? extends Job> jobClass)
            throws SchedulerException {
        for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
            scheduler.unscheduleJob(trigger.getKey());
        }

        log.info("Scheduling job [{}] with cron [{}]", jobKey.getName(), cronExpression);

        JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();

        Trigger trigger = TriggerBuilder.newTrigger()
                                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                                                                         .inTimeZone(TimeZone.getTimeZone("Europe/Rome")))
                                        .build();

        scheduler.scheduleJob(job, trigger);
    }
}
