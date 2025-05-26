package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeSummaryRepository;
import it.gov.pagopa.cgn.portal.service.BucketService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class SendWeeklyDiscountBucketCodesSummaryJob
        implements Job {

    private static final String JOB_LOG_NAME = "Send Weekly Discount Bucket Codes Summary Job";

    private final DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository;
    private final BucketService bucketService;

    @Autowired
    public SendWeeklyDiscountBucketCodesSummaryJob(DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository,
                                                   BucketService bucketService) {
        this.discountBucketCodeSummaryRepository = discountBucketCodeSummaryRepository;
        this.bucketService = bucketService;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void execute(JobExecutionContext context) {

        log.info(JOB_LOG_NAME + "started");
        Instant start = Instant.now();
        List<DiscountBucketCodeSummaryEntity> discountBucketCodeSummaryList = discountBucketCodeSummaryRepository.findAllPublishedNotExpired();

        if (!CollectionUtils.isEmpty(discountBucketCodeSummaryList)) {
            log.info("Found " + discountBucketCodeSummaryList.size() +
                     " not expired discount bucket code summaries to check");
            discountBucketCodeSummaryList.forEach(bucketService::checkDiscountBucketCodeSummaryExpirationAndSendNotification);
        }

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
    }

}
