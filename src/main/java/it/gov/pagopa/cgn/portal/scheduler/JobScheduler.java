package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service
@Slf4j
public class JobScheduler {

    private static final String DISCOUNTS_JOB_GROUP = "discounts";
    private final Scheduler scheduler;
    private final ParamFacade paramFacade;

    public JobScheduler(Scheduler scheduler, ParamFacade paramFacade) {
        this.scheduler = scheduler;
        this.paramFacade = paramFacade;
    }

    public void scheduleCheckExpiringDiscountsJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("check-expiring", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey, paramFacade.getCheckExpiringDiscountsJobCronExpression(), CheckExpiringDiscountsJob.class);
    }

    public void scheduleCheckAvailableDiscountBucketCodesJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("check-available-codes", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    paramFacade.getCheckAvailableDiscountBucketCodesJobCronExpression(),
                    CheckAvailableDiscountBucketCodesJob.class);
    }

    public void scheduleLowAvailableDiscountBucketCodesNotificationJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("low-available-codes-notification", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    paramFacade.getSendLowDiscountBucketCodesNotificationJobCronExpression(),
                    SendLowDiscountBucketCodesNotificationJob.class);
    }

    public void scheduleSuspendDiscountsWithoutAvailableBucketCodesJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("suspend-discount-with-expired-bucket", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    paramFacade.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression(),
                    SuspendDiscountsWithoutAvailableBucketCodesJob.class);
    }

    public void scheduleSendWeeklyDiscountBucketCodesSummaryJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("send-weekly-discount-bucket-codes-summary", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    paramFacade.getSendWeeklyDiscountBucketCodesSummaryJobCronExpression(),
                    SendWeeklyDiscountBucketCodesSummaryJob.class);
    }

    public void scheduleSendDiscountsToEycaJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("send-discount-to-eyca", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey, paramFacade.getSendDiscountsToEycaJobCronExpression(), SendDiscountsToEycaJob.class);
    }

    public void scheduleCleanDiscountsBucketCodesJob()
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("clean-discounts-bucket-codes", DISCOUNTS_JOB_GROUP);
        scheduleJob(jobKey,
                    paramFacade.getCleanDiscountsBucketCodesJobCronExpression(),
                    CleanDiscountsBucketCodesJob.class);
    }

    private void scheduleJob(JobKey jobKey, String cronExpression, Class<? extends Job> jobClass)
            throws SchedulerException {

        //deletes job and own triggers
        if (scheduler.checkExists(jobKey)) {
            log.info("Job [{}] it already exists, I'll delete it before recreating it", jobKey.getName());
            scheduler.deleteJob(jobKey);
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
