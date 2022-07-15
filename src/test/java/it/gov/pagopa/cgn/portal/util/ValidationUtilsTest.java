package it.gov.pagopa.cgn.portal.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ValidationUtilsTest {

    @Test
    public void IsNullOrEmptyOrBlank_Null_ReturnsTrue() {
        Assertions.assertTrue(ValidationUtils.isNullOrEmptyOrBlank(null));
    }

    @Test
    public void IsNullOrEmptyOrBlank_Empty_ReturnsTrue() {
        Assertions.assertTrue(ValidationUtils.isNullOrEmptyOrBlank(""));
    }

    @Test
    public void IsNullOrEmptyOrBlank_Blank_ReturnsTrue() {
        Assertions.assertTrue(ValidationUtils.isNullOrEmptyOrBlank("    "));
    }

    @Test
    public void IsNullOrEmptyOrBlank_String_ReturnsFalse() {
        Assertions.assertFalse(ValidationUtils.isNullOrEmptyOrBlank("  a string  "));
    }
}
