package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;
import it.gov.pagopa.cgn.portal.repository.BucketCodeLoadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import it.gov.pagopa.cgn.portal.service.BucketService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Component
public class BucketLoadUtils {

    private final BucketService bucketService;
    private final EntityManager entityManager;

    public BucketLoadUtils(BucketService bucketService, EntityManager entityManager) {
        this.bucketService = bucketService;
        this.entityManager = entityManager;
    }

    @Async("threadPoolTaskExecutor")
    public void TestAsyncDatabaseLock() throws InterruptedException {
        log.info("#LOCK STARTED");
        try {
            entityManager
                    .createNativeQuery("BEGIN WORK; LOCK bucket_code_load IN ACCESS EXCLUSIVE MODE; SELECT pg_sleep(10); COMMIT WORK; SELECT '1' AS DONE;")
                    .getSingleResult();
        } catch (Exception ex) {
            // nope
        }
        log.info("#LOCK ENDED");
    }

    @Async("threadPoolTaskExecutor")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public void storeCodesBucket(Long discountId) {
        log.info("#ASYNC");
        bucketService.setRunningBucketLoad(discountId);
        bucketService.performBucketLoad(discountId);
    }
}
