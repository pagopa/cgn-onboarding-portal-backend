package it.gov.pagopa.cgn.portal.email;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailNotificationFacadeWeekNumberTest {

    @Test
    void weekOfYear_usesWeekBasedYearNumberAtYearBoundaries() {
        assertEquals(53,
                     EmailNotificationFacade.weekOfYear(LocalDate.of(2027, Month.JANUARY, 1), Locale.ITALY));
        assertEquals(1,
                     EmailNotificationFacade.weekOfYear(LocalDate.of(2026, Month.DECEMBER, 28), Locale.US));
    }
}
