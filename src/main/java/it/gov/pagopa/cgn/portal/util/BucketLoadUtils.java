package it.gov.pagopa.cgn.portal.util;

import javax.transaction.Transactional;

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
    public void storeCodesBucket(Long discountId) {
        bucketService.setRunningBucketLoad(discountId);
        bucketService.performBucketLoad(discountId);
    }
}
