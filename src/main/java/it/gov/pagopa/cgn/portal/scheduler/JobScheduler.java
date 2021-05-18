package it.gov.pagopa.cgn.portal.scheduler;

import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service
public class JobScheduler {

    private final Scheduler scheduler;

    public JobScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleCheckExpiringDiscountsJob() throws SchedulerException {

        JobKey jobKey = JobKey.jobKey("check-expiring", "discounts");

        for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
            scheduler.unscheduleJob(trigger.getKey());
        }

        JobDetail job = JobBuilder
                .newJob(CheckExpiringDiscountsJob.class)
                .withIdentity(jobKey)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ? * ")
                        .inTimeZone(TimeZone.getTimeZone("Europe/Rome")))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
