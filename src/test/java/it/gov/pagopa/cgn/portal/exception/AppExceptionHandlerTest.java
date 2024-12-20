package it.gov.pagopa.cgn.portal.exception;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.config.AppExceptionHandler;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

@SpringBootTest
@ActiveProfiles({"dev"})
public class AppExceptionHandlerTest
        extends IntegrationAbstractTest {

    @Test
    public void TestInternalError_ThrowGenericException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        Exception ex = new InternalErrorException("Test internal");
        Assert.assertEquals(ex.getMessage(), handler.handleInternalErrorException(ex).getBody());
    }

    @Test
    public void TestInternalError_ThrowRestClientException() {
        AppExceptionHandler handler = new AppExceptionHandler();
        Exception ex = new RestClientException("Test rest client exception");
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
