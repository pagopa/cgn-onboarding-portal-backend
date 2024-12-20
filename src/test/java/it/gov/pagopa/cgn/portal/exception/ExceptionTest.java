package it.gov.pagopa.cgn.portal.exception;

import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ExceptionTest {

    private static final String NPE_ERROR_MSG = "NULL for test";

    @Test
    public void CGNExceptionConstructor_ConstructorWithThrowable_Ok() {
        RuntimeException runtimeException = new CGNException(new NullPointerException(NPE_ERROR_MSG));
        Assertions.assertTrue(runtimeException.getMessage().contains(NullPointerException.class.getName()));
        Assertions.assertTrue(runtimeException.getMessage().contains(NPE_ERROR_MSG));
    }

    @Test
    public void CGNExceptionConstructor_ConstructorWithMessage_Ok() {
        RuntimeException runtimeException = new CGNException(NPE_ERROR_MSG);
        Assertions.assertEquals(NPE_ERROR_MSG, runtimeException.getMessage());
    }

    @Test
    public void CGNExceptionConstructor_ConstructorWithMessageAndThrowable_Ok() {
        RuntimeException runtimeException = new CGNException(NPE_ERROR_MSG, new NullPointerException());
        Assertions.assertTrue(runtimeException.getCause() instanceof NullPointerException);
        Assertions.assertEquals(NPE_ERROR_MSG, runtimeException.getMessage());
    }

    @Test
    public void InvalidRequestExceptionConstructor_ConstructorWithImageDimensionNotValid_Ok() {
        InvalidRequestException irException = new InvalidRequestException(ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue());
        Assertions.assertEquals(ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue(), irException.getMessage());
    }

    @Test
    public void InvalidRequestExceptionConstructor_ConstructorWithImageDataNotValid_Ok() {
        InvalidRequestException irException = new InvalidRequestException(ErrorCodeEnum.IMAGE_DATA_NOT_VALID.getValue());
        Assertions.assertEquals(ErrorCodeEnum.IMAGE_DATA_NOT_VALID.getValue(), irException.getMessage());
    }

    @Test
    public void ImageException_ImageExceptionToStringContainsCodeAndMessage_Ok() {
        InvalidRequestException irException = new InvalidRequestException(ErrorCodeEnum.IMAGE_DATA_NOT_VALID.getValue());
        Assertions.assertTrue(irException.toString().contains(ErrorCodeEnum.IMAGE_DATA_NOT_VALID.getValue()));
    }

}
