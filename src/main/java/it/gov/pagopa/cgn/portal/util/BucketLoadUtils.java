package it.gov.pagopa.cgn.portal.util;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import it.gov.pagopa.cgn.portal.service.BucketService;

@Component
public class BucketLoadUtils {

    private final BucketService bucketService;

    public BucketLoadUtils(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Async("threadPoolTaskExecutor")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 1.5))
    public void storeCodesBucket(Long discountId) {
        bucketService.setRunningBucketLoad(discountId);
        bucketService.performBucketLoad(discountId);
    }
}
