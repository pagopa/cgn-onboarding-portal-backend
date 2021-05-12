package it.gov.pagopa.cgn.portal.exception;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CGNExceptionTest {

    private static final String NPE_ERROR_MSG = "NULL for test";

    @Test
    public void Constructor_ConstructorWithThrowable_Ok() {
        RuntimeException runtimeException = new CGNException(new NullPointerException(NPE_ERROR_MSG));
        Assertions.assertTrue(runtimeException.getMessage().contains(NullPointerException.class.getName()));
        Assertions.assertTrue(runtimeException.getMessage().contains(NPE_ERROR_MSG));
    }

    @Test
    public void Constructor_ConstructorWithMessage_Ok() {
        RuntimeException runtimeException = new CGNException(NPE_ERROR_MSG);
        Assertions.assertEquals(NPE_ERROR_MSG, runtimeException.getMessage());
    }

    @Test
    public void Constructor_ConstructorWithMessageAndThrowable_Ok() {
        RuntimeException runtimeException = new CGNException(NPE_ERROR_MSG, new NullPointerException());
        Assertions.assertTrue(runtimeException.getCause() instanceof NullPointerException);
        Assertions.assertEquals(NPE_ERROR_MSG, runtimeException.getMessage());
    }
}
