package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@SpringBootTest
@ActiveProfiles({"dev"})
class SuspendDiscountsWithoutAvailableBucketCodesJobTest extends IntegrationAbstractTest {

    @Autowired
    private SuspendDiscountsWithoutAvailableBucketCodesJob job;

    @Autowired
    private ConfigProperties configProperties;

    private DiscountEntity discountEntity;

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


    private void init() throws IOException {
        setAdminAuth();

        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        discountEntity = testObject.getDiscountEntityList().get(0);
        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        discountEntity.setEndDate(LocalDate.now().plusDays(3));
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        discountRepository.save(discountEntity);
        bucketService.createEmptyDiscountBucketCodeSummary(discountEntity);
    }

    private void testJob(DiscountStateEnum expectedState) {
        job.execute(null);
        var maybeSuspendedDiscount = discountRepository.findById(discountEntity.getId()).orElseThrow();
        Assertions.assertEquals(expectedState, maybeSuspendedDiscount.getState());
    }
}
