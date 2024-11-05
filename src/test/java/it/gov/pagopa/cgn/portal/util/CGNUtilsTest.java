package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RunWith(SpringRunner.class)
public class CGNUtilsTest {


    @Test
    public void ValidateImage_ValidateInvalidImage_InvalidRequestException() {
        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", new byte[10]);

        Exception exception =  Assert.assertThrows(InternalErrorException.class,
                () -> CGNUtils.validateImage(multipartFile,800, 600));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_DATA_NOT_VALID.getValue()));
    }

    @Test
    public void ValidateImage_ValidateImageWithoutFileName_InvalidRequestException() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", null, "image/png", image);

        Exception exception =  Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.validateImage(multipartFile,800, 600));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue()));
    }

    @Test
    public void ValidateImage_ValidateImageWithTooSmallResolutionParams_InvalidRequestException() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", image);

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.validateImage(multipartFile,2000, 2000));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue()));
    }

    @Test
    public void ValidateImage_ValidateImageWithTheHeightTooSmallResolutionParams_InvalidRequestException() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", image);

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.validateImage(multipartFile,50, 2000));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue()));
    }

    @Test
    public void ValidateImage_ValidateImageWithTheWidthTooSmallResolutionParams_InvalidRequestException() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", image);

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.validateImage(multipartFile,2000, 50));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue()));
    }

    @Test
    public void ValidateImageFile_ValidateImageFileWithInvalidParams_InvalidRequestException()  {

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfImageFile("filename.pdf"));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue()));

        exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfImageFile("filename"));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue()));
    }

    @Test
    public void ValidatePDFFile_ValidatePDFFileWithInvalidParams_InvalidRequestException()  {
        Exception exception =  Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfPdfFile("filename.png"));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.PDF_NAME_OR_EXTENSION_NOT_VALID.getValue()));

        exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfPdfFile("filename"));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.PDF_NAME_OR_EXTENSION_NOT_VALID.getValue()));
    }

    @Test
    public void ValidateCSVFile_ValidateCSVFileWithInvalidParams_InvalidRequestException()  {
        Exception exception =  Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfCsvFile("filename.xxx"));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.CSV_NAME_OR_EXTENSION_NOT_VALID.getValue()));

        exception = Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfCsvFile("filename"));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.CSV_NAME_OR_EXTENSION_NOT_VALID.getValue()));
    }

    @Test
    public void GetJwtToken_GetJwtToken_Ok()  {
        TestUtils.setAdminAuth();
        Assert.assertNotNull(CGNUtils.getJwtAdminUser());
        Assert.assertNotNull(CGNUtils.getJwtAdminUserName());
        TestUtils.setOperatorAuth();
        Assert.assertNotNull(CGNUtils.getJwtOperatorUser());
        Assert.assertNotNull(CGNUtils.getJwtOperatorUserId());
    }

    @Test
    public void GetJwtOperatorUser_GetJwtOperatorUserWithAdminToken_CGNException()  {
        TestUtils.setAdminAuth();
        Assert.assertThrows(CGNException.class, CGNUtils::getJwtOperatorUser);
    }

    @Test
    public void GetJwtAdminUser_GetJwtAdminUserWithUserToken_CGNException()  {
        TestUtils.setOperatorAuth();
        Assert.assertThrows(CGNException.class, CGNUtils::getJwtAdminUserName);
    }
}
