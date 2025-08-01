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
    }
}

