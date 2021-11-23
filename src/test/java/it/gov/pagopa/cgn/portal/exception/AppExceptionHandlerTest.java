package it.gov.pagopa.cgn.portal.exception;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.config.AppExceptionHandler;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.model.ImageErrorCode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@SpringBootTest
@ActiveProfiles({ "dev" })
public class AppExceptionHandlerTest extends IntegrationAbstractTest {

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

    @Test
    public void TestGenericInternalError_ThrowSpecificException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        ConfigProperties configProperties = new ConfigProperties();
        ReflectionTestUtils.setField(configProperties, "activeProfile", "dev");
        Exception ex = new InternalErrorException("Test generic internal");
        ReflectionTestUtils.setField(handler, "configProperties", configProperties);
        Assert.assertEquals(ex.getMessage(), handler.handleException(ex).getBody());
    }

    @Test
    public void TestGenericInternalError_ThrowGenericException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        ConfigProperties configProperties = new ConfigProperties();
        ReflectionTestUtils.setField(configProperties, "activeProfile", "nodev");
        ReflectionTestUtils.setField(configProperties, "exceptionGenericMessage", "generic message");
        Exception ex = new InternalErrorException("Test generic internal");
        ReflectionTestUtils.setField(handler, "configProperties", configProperties);
        Assert.assertEquals("generic message", handler.handleException(ex).getBody());
    }
}
