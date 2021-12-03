package it.gov.pagopa.cgn.portal.util;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import it.gov.pagopa.cgn.portal.service.BucketService;

import java.io.IOException;
import java.util.stream.Stream;

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
