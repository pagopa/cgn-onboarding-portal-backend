package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class SendDiscountsToEycaJob implements Job {

    private final ExportService exportService;
    private final ConfigProperties configProperties;

    private static final String JOB_LOG_NAME = "Send Discounts to EYCA Job ";


    @Autowired
    public SendDiscountsToEycaJob(ExportService exportService, ConfigProperties configProperties){
        this.exportService = exportService;
      this.configProperties = configProperties;
    }


    public void execute(JobExecutionContext context) {

        Optional<Boolean> eycaExportEnabled = Optional.ofNullable(configProperties.getEycaExportEnabled());
        if (eycaExportEnabled.isPresent()&& Boolean.FALSE.equals(eycaExportEnabled.get())) {
            return;
        }

        log.info(JOB_LOG_NAME + "started");
        Instant start = Instant.now();

        exportService.sendDiscountsToEyca();

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
    }

}
