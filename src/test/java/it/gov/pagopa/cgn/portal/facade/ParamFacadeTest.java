package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.service.ParamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParamFacadeTest {

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private ParamService paramService;

    private ParamFacade paramFacade;

    @Nested
    class DevProfile {

        @BeforeEach
        void setUp() {
            when(configProperties.isActiveProfileDev()).thenReturn(true);
            paramFacade = new ParamFacade(paramService, configProperties);
        }

        @Test
        void shouldReturnEycaJobMailToFromConfig() {
            when(configProperties.getEycaJobMailTo()).thenReturn("a@x.it;b@y.it");

            String[] result = paramFacade.getEycaJobMailTo();

            assertArrayEquals(new String[]{"a@x.it", "b@y.it"}, result);
        }

        @Test
        void shouldReturnCheckExpiringDiscountsJobDaysFromConfig() {
            when(configProperties.getCheckExpiringDiscountsJobDays()).thenReturn(5);

            int result = paramFacade.getCheckExpiringDiscountsJobDays();

            assertEquals(5, result);
        }

        @Test
        void shouldReturnInactivateAgreementsJobExpiredStaleMonthsFromConfig() {
            when(configProperties.getInactivateAgreementsJobExpiredStaleMonths()).thenReturn(6);

            int result = paramFacade.getInactivateAgreementsJobExpiredStaleMonths();

            assertEquals(6, result);
        }

        @Test
        void shouldReturnEycaAdminMailToFromConfig() {
            when(configProperties.getEycaAdminMailTo()).thenReturn("a@x.it;b@y.it");

            assertArrayEquals(new String[]{"a@x.it", "b@y.it"}, paramFacade.getEycaAdminMailTo());
        }

        @Test
        void shouldReturnEycaRateLimitValuesFromConfig() {
            when(configProperties.getEycaRateLimitMaxRetries()).thenReturn(3);
            when(configProperties.getEycaRateLimitWaitMs()).thenReturn(5000L);

            assertEquals(3, paramFacade.getEycaRateLimitMaxRetries());
            assertEquals(5000L, paramFacade.getEycaRateLimitWaitMs());
        }

        @Test
        void shouldReturnJobCronExpressionsFromConfig() {
            when(configProperties.getCheckExpiringDiscountsJobCronExpression()).thenReturn("c1");
            when(configProperties.getCheckAvailableDiscountBucketCodesJobCronExpression()).thenReturn("c2");
            when(configProperties.getSendLowDiscountBucketCodesNotificationJobCronExpression()).thenReturn("c3");
            when(configProperties.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression()).thenReturn("c4");
            when(configProperties.getSendWeeklyDiscountBucketCodesSummaryJobCronExpression()).thenReturn("c5");
            when(configProperties.getSendDiscountsToEycaJobCronExpression()).thenReturn("c6");
            when(configProperties.getCleanDiscountsBucketCodesJobCronExpression()).thenReturn("c7");
            when(configProperties.getInactivateAgreementsJobCronExpression()).thenReturn("c8");

            assertEquals("c1", paramFacade.getCheckExpiringDiscountsJobCronExpression());
            assertEquals("c2", paramFacade.getCheckAvailableDiscountBucketCodesJobCronExpression());
            assertEquals("c3", paramFacade.getSendLowDiscountBucketCodesNotificationJobCronExpression());
            assertEquals("c4", paramFacade.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression());
            assertEquals("c5", paramFacade.getSendWeeklyDiscountBucketCodesSummaryJobCronExpression());
            assertEquals("c6", paramFacade.getSendDiscountsToEycaJobCronExpression());
            assertEquals("c7", paramFacade.getCleanDiscountsBucketCodesJobCronExpression());
            assertEquals("c8", paramFacade.getInactivateAgreementsJobCronExpression());
        }

        @Test
        void shouldReturnSuspendReferentsMailSendingFromConfig() {
            when(configProperties.getSuspendReferentsMailSending()).thenReturn("true");

            assertEquals("true", paramFacade.getSuspendReferentsMailSending());
        }
    }

    @Nested
    class ProdProfile {

        @BeforeEach
        void setUp() {
            when(configProperties.isActiveProfileDev()).thenReturn(false);
            paramFacade = new ParamFacade(paramService, configProperties);
        }

        @Test
        void shouldReturnEycaJobMailToFromParamService() {
            when(paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "eyca.job.mailto"))
                    .thenReturn("admin@eyca.org;info@eyca.org");

            String[] result = paramFacade.getEycaJobMailTo();

            assertArrayEquals(new String[]{"admin@eyca.org", "info@eyca.org"}, result);
        }

        @Test
        void shouldReturnCheckExpiringDiscountsJobDaysFromParamService() {
            when(paramService.getParam(ParamGroupEnum.CHECK_EXPIRING_DISC_JOB, "check.expiring.discounts.job.days"))
                    .thenReturn("7");

            int result = paramFacade.getCheckExpiringDiscountsJobDays();

            assertEquals(7, result);
        }

        @Test
        void shouldReturnInactivateAgreementsJobExpiredStaleMonthsFromParamService() {
            when(paramService.getParam(ParamGroupEnum.INACTIVATE_AGREEMENTS_JOB,
                                       "inactivate.agreements.job.expired.stale.months"))
                    .thenReturn("6");

            int result = paramFacade.getInactivateAgreementsJobExpiredStaleMonths();

            assertEquals(6, result);
        }

        @Test
        void shouldReturnEycaAdminMailToFromParamService() {
            when(paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "eyca.admin.mailto"))
                    .thenReturn("a@x.it;b@y.it");

            assertArrayEquals(new String[]{"a@x.it", "b@y.it"}, paramFacade.getEycaAdminMailTo());
        }

        @Test
        void shouldReturnEycaRateLimitValuesFromParamService() {
            when(paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "eyca.export.rate-limit.max-retries"))
                    .thenReturn("3");
            when(paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "eyca.export.rate-limit.wait-ms"))
                    .thenReturn("5000");

            assertEquals(3, paramFacade.getEycaRateLimitMaxRetries());
            assertEquals(5000L, paramFacade.getEycaRateLimitWaitMs());
        }

        @Test
        void shouldReturnJobCronExpressionsFromParamService() {
            when(paramService.getParam(ParamGroupEnum.CHECK_EXPIRING_DISC_JOB, "check.expiring.discounts.job.cron"))
                    .thenReturn("c1");
            when(paramService.getParam(ParamGroupEnum.CHECK_AVAILABLE_DISC_JOB,
                                       "check.available.discounts.bucket.codes.job.cron")).thenReturn("c2");
            when(paramService.getParam(ParamGroupEnum.SEND_LOW_DISC_BUCKET_CODES_NOTIF_JOB,
                                       "send.low.bucket.codes.notification.job.cron")).thenReturn("c3");
            when(paramService.getParam(ParamGroupEnum.SUSPEND_DISCOUNTS_JOB,
                                       "suspend.discounts.without.available.bucket.codes.job.cron")).thenReturn("c4");
            when(paramService.getParam(ParamGroupEnum.SEND_WEEKLY_SUMMARY_JOB,
                                       "send.weekly.discount.bucket.codes.summary.job.cron")).thenReturn("c5");
            when(paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "send.discounts.to.eyca.job.cron"))
                    .thenReturn("c6");
            when(paramService.getParam(ParamGroupEnum.CLEAN_DISCOUNTS_BUCKET_CODES_JOB,
                                       "clean.discounts.bucket.codes.job.cron")).thenReturn("c7");
            when(paramService.getParam(ParamGroupEnum.INACTIVATE_AGREEMENTS_JOB, "inactivate.agreements.job.cron"))
                    .thenReturn("c8");

            assertEquals("c1", paramFacade.getCheckExpiringDiscountsJobCronExpression());
            assertEquals("c2", paramFacade.getCheckAvailableDiscountBucketCodesJobCronExpression());
            assertEquals("c3", paramFacade.getSendLowDiscountBucketCodesNotificationJobCronExpression());
            assertEquals("c4", paramFacade.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression());
            assertEquals("c5", paramFacade.getSendWeeklyDiscountBucketCodesSummaryJobCronExpression());
            assertEquals("c6", paramFacade.getSendDiscountsToEycaJobCronExpression());
            assertEquals("c7", paramFacade.getCleanDiscountsBucketCodesJobCronExpression());
            assertEquals("c8", paramFacade.getInactivateAgreementsJobCronExpression());
        }

        @Test
        void shouldReturnSuspendReferentsMailSendingFromParamService() {
            when(paramService.getParam(ParamGroupEnum.CGN_JOB_FLAGS, "suspend.referents.mail.sending"))
                    .thenReturn("true");

            assertEquals("true", paramFacade.getSuspendReferentsMailSending());
        }
    }
}

