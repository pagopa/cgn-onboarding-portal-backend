package it.gov.pagopa.cgn.portal.exception;

import it.gov.pagopa.cgn.portal.config.AppExceptionHandler;
import it.gov.pagopa.cgnonboardingportal.model.ImageErrorCode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RunWith(SpringRunner.class)
public class AppExceptionHandlerTest {

    @Test
    public void TestMaxUploadSizeExceededException_ThrowMaxUploadSizeExceededException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        Assert.assertEquals(ImageErrorCode.IMAGE_SIZE_EXCEEDED.getValue(),
                handler.handleImageError(new MaxUploadSizeExceededException(1024 * 1024 * 5)).getBody());
    }

    @Test
    public void TestGenericError_ThrowGenericException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        Assert.assertEquals(ImageErrorCode.GENERIC.getValue(),
                handler.handleImageError(new NullPointerException()).getBody());
    }

    @Test
    public void TestConflictError_ThrowGenericException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        Exception ex = new ConflictErrorException("Test null");
        Assert.assertEquals(ex.getMessage(), handler.handleConflictErrorException(ex).getBody());
    }

    @Test
    public void TestInternalError_ThrowGenericException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        Exception ex = new InternalErrorException("Test internal");
        Assert.assertEquals(ex.getMessage(), handler.handleInternalErrorException(ex).getBody());
    }
}
