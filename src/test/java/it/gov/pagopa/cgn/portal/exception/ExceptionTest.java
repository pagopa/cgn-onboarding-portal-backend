package it.gov.pagopa.cgn.portal.exception;

import it.gov.pagopa.cgnonboardingportal.model.ImageErrorCode;
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
    public void ImageExceptionConstructor_ConstructorWithImageErrorCodeEnum_Ok() {
        ImageException imageException = new ImageException(ImageException.ImageErrorCodeEnum.INVALID_FORMAT);
        Assertions.assertEquals(ImageErrorCode.INVALID_FORMAT.getValue(), imageException.getImageErrorCodeDto());
        Assertions.assertEquals(ImageException.ImageErrorCodeEnum.INVALID_FORMAT.getDefaultErrorMsg(), imageException.getMessage());
    }

    @Test
    public void ImageExceptionConstructor_ConstructorWithImageErrorCodeEnumAndMessage_Ok() {
        String customMsg = "an error";
        ImageException imageException = new ImageException(ImageException.ImageErrorCodeEnum.GENERIC, customMsg);
        Assertions.assertEquals(ImageErrorCode.GENERIC.getValue(), imageException.getImageErrorCodeDto());
        Assertions.assertEquals(customMsg, imageException.getMessage());
    }

}
