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

        for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
            scheduler.unscheduleJob(trigger.getKey());
        }

        JobDetail job = JobBuilder
                .newJob(CheckExpiringDiscountsJob.class)
                .withIdentity(jobKey)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(configProperties.getExpiringDiscountsJobCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("Europe/Rome")))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
