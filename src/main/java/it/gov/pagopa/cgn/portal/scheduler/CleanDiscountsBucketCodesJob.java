package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.service.BucketService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class CleanDiscountsBucketCodesJob
        implements Job {

    private static final String JOB_LOG_NAME = "Clean Discounts Bucket Codes Job";

    private final BucketService bucketService;

    @Autowired
    public CleanDiscountsBucketCodesJob(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void execute(JobExecutionContext context) {

        log.info(JOB_LOG_NAME + " started");
        Instant start = Instant.now();

        bucketService.deleteAllBucketCodesUsedBeforeRetentionPeriod();

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in {} seconds", Duration.between(start, end).getSeconds());
    }

}
