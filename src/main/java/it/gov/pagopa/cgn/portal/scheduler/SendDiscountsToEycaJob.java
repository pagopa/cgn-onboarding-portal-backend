package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class SendDiscountsToEycaJob implements Job {

    private final ExportService exportService;
    private static final String JOB_LOG_NAME = "Send Discounts to EYCA Job ";


    @Autowired
    public SendDiscountsToEycaJob(ExportService exportService){
        this.exportService = exportService;
    }


    public void execute(JobExecutionContext context) {

        log.info(JOB_LOG_NAME + "started");
        Instant start = Instant.now();

        exportService.sendDiscountsToEyca();

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
    }

}
