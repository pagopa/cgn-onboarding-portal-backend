package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.ImageException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
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
        Assert.assertThrows(ImageException.class,
                () -> CGNUtils.validateImage(multipartFile,800, 600));
    }

    @Test
    public void ValidateImage_ValidateImageWithoutFileName_InvalidRequestException() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", null, "image/png", image);
        Assert.assertThrows(ImageException.class,
                () -> CGNUtils.validateImage(multipartFile,800, 600));
    }

    @Test
    public void ValidateImage_ValidateImageWithTooBigResolutionParams_InvalidRequestException() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", image);
        Assert.assertThrows(ImageException.class,
                () -> CGNUtils.validateImage(multipartFile,2000, 2000));

    }

    @Test
    public void ValidateImageFile_ValidateImageFileWithInvalidParams_InvalidRequestException()  {
        Assert.assertThrows(ImageException.class,
                () -> CGNUtils.checkIfImageFile("filename.pdf"));
        Assert.assertThrows(ImageException.class,
                () -> CGNUtils.checkIfImageFile("filename"));
    }

    @Test
    public void ValidatePDFFile_ValidatePDFFileWithInvalidParams_InvalidRequestException()  {
        Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfPdfFile("filename.png"));
        Assert.assertThrows(InvalidRequestException.class,
                () -> CGNUtils.checkIfPdfFile("filename"));
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
