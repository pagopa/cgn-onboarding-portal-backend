package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class SendDiscountsToEycaJob
        implements Job {

    private static final String JOB_LOG_NAME = "Send Discounts to EYCA Job ";
    private final ExportService exportService;


    @Autowired
    public SendDiscountsToEycaJob(ExportService exportService) {
        this.exportService = exportService;
    }


    @Override
    public void execute(JobExecutionContext context) {

        log.info(JOB_LOG_NAME + "started");
        Instant start = Instant.now();

        exportService.sendDiscountsToEyca();

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in {} seconds", Duration.between(start, end).getSeconds());
    }

}
