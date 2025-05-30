package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeSummaryRepository;
import it.gov.pagopa.cgn.portal.service.BucketService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.*;

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

            Map<ProfileEntity, List<Map<String, Long>>> groupedListDiscountsByProfile = groupDiscountsByProfile(
                    discountBucketCodeSummaryList);

            groupedListDiscountsByProfile.forEach(bucketService::notifyWeeklyMerchantDiscountBucketCodesSummary);
        }

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
    }

    public Map<ProfileEntity, List<Map<String, Long>>> groupDiscountsByProfile(List<DiscountBucketCodeSummaryEntity> summaries) {
        return summaries.stream()
                        .collect(Collectors.groupingBy(s -> s.getDiscount().getAgreement().getProfile(),
                                                       Collectors.mapping(s -> Map.of(s.getDiscount().getName(),
                                                                                      s.getAvailableCodes()),
                                                                          Collectors.toList())));
    }
}
