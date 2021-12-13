package it.gov.pagopa.cgn.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import it.gov.pagopa.cgn.portal.service.BucketService;

@Slf4j
@Component
public class BucketLoadUtils {

    private final BucketService bucketService;

    public BucketLoadUtils(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Async("threadPoolTaskExecutor")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public void storeCodesBucket(Long discountId) {
        log.trace("Starting asynchronous bucket codes loading.");
        bucketService.setRunningBucketLoad(discountId);
        bucketService.performBucketLoad(discountId);
    }

    @Async("threadPoolTaskExecutor")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public void deleteBucketCodes(Long discountId) {
        log.trace("Starting asynchronous bucket codes delete.");
        bucketService.deleteBucketCodes(discountId);
    }
}
