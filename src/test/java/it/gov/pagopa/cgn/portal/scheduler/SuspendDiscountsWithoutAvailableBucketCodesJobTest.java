package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@ActiveProfiles({"dev"})
class SuspendDiscountsWithoutAvailableBucketCodesJobTest extends IntegrationAbstractTest {

    @Autowired
    private SuspendDiscountsWithoutAvailableBucketCodesJob job;

    @Autowired
    private ConfigProperties configProperties;

    private DiscountEntity discountEntity;

    private AgreementEntity agreementEntity;

    @Test
    void Execute_ExecuteJob_NoExpiredBucketCodeSummaries() {
        Assertions.assertDoesNotThrow(() -> job.execute(null));
    }

    @Test
    void Execute_ExecuteJob_DoNotSuspendDiscountIfGivenDaysNotPassed() throws IOException {
        init();
        discountBucketCodeSummaryRepository.findAll().forEach(s -> {
            s.setAvailableCodes(1L);
            s.setExpiredAt(OffsetDateTime.now());
            discountBucketCodeSummaryRepository.save(s);
        });
        testJob(DiscountStateEnum.PUBLISHED);
    }

    @Test
    void Execute_ExecuteJob_SuspendDiscountIfGivenDaysPassed() throws IOException {
        init();
        discountBucketCodeSummaryRepository.findAll().forEach(s -> {
            s.setAvailableCodes(1L);
            s.setExpiredAt(OffsetDateTime.now().minusDays(configProperties.getSuspendDiscountsWithoutAvailableBucketCodesAfterDays()));
            discountBucketCodeSummaryRepository.save(s);
        });
        testJob(DiscountStateEnum.SUSPENDED);
    }

    @Test
    void Execute_ExecuteJob_CheckMaterializedViews() throws IOException {
        init();

        // refresh materialized views
        onlineMerchantRepository.refreshView();

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() >= 1);

        // assert the merchant is in the view
        var onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(1, onlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), onlineMerchantEntities.get(0).getId());

        discountBucketCodeSummaryRepository.findAll().forEach(s -> {
            s.setAvailableCodes(1L);
            s.setExpiredAt(OffsetDateTime.now().minusDays(configProperties.getSuspendDiscountsWithoutAvailableBucketCodesAfterDays()));
            discountBucketCodeSummaryRepository.save(s);
        });

        testJob(DiscountStateEnum.SUSPENDED);

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() <= 0);

        // assert the merchant is not in the view anymore
        onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(0, onlineMerchantEntities.size());
    }


    private void init() throws IOException {
        setAdminAuth();

        AgreementTestObject testObject = createApprovedAgreement();
        agreementEntity = testObject.getAgreementEntity();
        discountEntity = testObject.getDiscountEntityList().get(0);
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        discountRepository.save(discountEntity);
        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        bucketService.createEmptyDiscountBucketCodeSummary(discountEntity);
    }

    private void testJob(DiscountStateEnum expectedState) {
        job.execute(null);
        var maybeSuspendedDiscount = discountRepository.findById(discountEntity.getId()).orElseThrow();
        Assertions.assertEquals(expectedState, maybeSuspendedDiscount.getState());
    }
}
