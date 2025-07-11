package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeSummaryRepository;
import it.gov.pagopa.cgn.portal.service.DiscountService;
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
public class SuspendDiscountsWithoutAvailableBucketCodesJob
        implements Job {

    private static final String JOB_LOG_NAME = "Suspend Discounts Without Available Bucket Codes Job";

    private final DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository;
    private final DiscountService discountService;


    @Autowired
    public SuspendDiscountsWithoutAvailableBucketCodesJob(DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository,
                                                          DiscountService discountService) {
        this.discountBucketCodeSummaryRepository = discountBucketCodeSummaryRepository;
        this.discountService = discountService;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void execute(JobExecutionContext context) {

        log.info(JOB_LOG_NAME + "started");
        Instant start = Instant.now();
        List<DiscountBucketCodeSummaryEntity> discountBucketCodeSummaryList = discountBucketCodeSummaryRepository.findAllPublishedAndExpired();

        if (!CollectionUtils.isEmpty(discountBucketCodeSummaryList)) {
            log.info("Found " + discountBucketCodeSummaryList.size() +
                     " expired discount bucket code summaries to check");
            discountBucketCodeSummaryList.forEach(discountService::suspendDiscountIfDiscountBucketCodesAreExpired);
        }

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
    }

}
