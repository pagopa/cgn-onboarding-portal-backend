package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(JUnitParamsRunner.class)
public class CGNUtilsTest {

    @Test
    @Parameters({"2000, 2000", "50, 2000", "2000, 50"})
    public void validateImage_InvalidDimensions_ThrowsInvalidRequestException(int width, int height)
            throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", image);

        Exception exception = assertThrows(InvalidRequestException.class,
                                           () -> CGNUtils.validateImage(multipartFile, width, height));

        assertEquals(ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue(), exception.getMessage());

    }

    @Test
    public void ValidateImage_ValidateImageWithoutFileName_InvalidRequestException()
            throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        MultipartFile multipartFile = new MockMultipartFile("fileItem", null, "image/png", image);

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                                                  () -> CGNUtils.validateImage(multipartFile, 800, 600));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue());
    }

    @Test
    public void ValidateImageFile_ValidateImageFileWithInvalidParams_InvalidRequestException() {

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                                                  () -> CGNUtils.checkIfImageFile("filename.pdf"));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue());

        exception = Assert.assertThrows(InvalidRequestException.class, () -> CGNUtils.checkIfImageFile("filename"));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue());
    }

    @Test
    public void ValidateImageFile_ValidateImageFileWithWeightGreatherThenFiveMB_InvalidRequestException()
            throws IOException {

        //just over 5MB
        byte[] image = CGNUtils.getFakeImage(20000, 17000);

        MultipartFile multipartFile = new MockMultipartFile("fileItem", "test-image.jpeg", "image/png", image);

        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                                                  () -> CGNUtils.validateImage(multipartFile, 800, 600));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.IMAGE_DIMENSION_NOT_VALID.getValue());
    }

    @Test
    public void ValidatePDFFile_ValidatePDFFileWithInvalidParams_InvalidRequestException() {
        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                                                  () -> CGNUtils.checkIfPdfFile("filename.png"));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.PDF_NAME_OR_EXTENSION_NOT_VALID.getValue());

        exception = Assert.assertThrows(InvalidRequestException.class, () -> CGNUtils.checkIfPdfFile("filename"));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.PDF_NAME_OR_EXTENSION_NOT_VALID.getValue());
    }

    @Test
    public void ValidateCSVFile_ValidateCSVFileWithInvalidParams_InvalidRequestException() {
        Exception exception = Assert.assertThrows(InvalidRequestException.class,
                                                  () -> CGNUtils.checkIfCsvFile("filename.xxx"));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.CSV_NAME_OR_EXTENSION_NOT_VALID.getValue());

        exception = Assert.assertThrows(InvalidRequestException.class, () -> CGNUtils.checkIfCsvFile("filename"));

        Assert.assertEquals(exception.getMessage(),
                            exception.getMessage(),
                            ErrorCodeEnum.CSV_NAME_OR_EXTENSION_NOT_VALID.getValue());
    }

    @Test
    public void ValidateImageFile_NullFileName_InvalidRequestException() {
        Exception exception = Assert.assertThrows(InvalidRequestException.class, () -> CGNUtils.checkIfImageFile(null));

        Assert.assertEquals(ErrorCodeEnum.IMAGE_NAME_OR_EXTENSION_NOT_VALID.getValue(), exception.getMessage());
    }

    @Test
    public void ValidatePDFFile_NullFileName_InvalidRequestException() {
        Exception exception = Assert.assertThrows(InvalidRequestException.class, () -> CGNUtils.checkIfPdfFile(null));

        Assert.assertEquals(ErrorCodeEnum.PDF_NAME_OR_EXTENSION_NOT_VALID.getValue(), exception.getMessage());
    }

    @Test
    public void ValidateCSVFile_NullFileName_InvalidRequestException() {
        Exception exception = Assert.assertThrows(InvalidRequestException.class, () -> CGNUtils.checkIfCsvFile(null));

        Assert.assertEquals(ErrorCodeEnum.CSV_NAME_OR_EXTENSION_NOT_VALID.getValue(), exception.getMessage());
    }


    @Test
    public void GetJwtToken_GetJwtToken_Ok() {
        TestUtils.setAdminAuth();
        Assert.assertNotNull(CGNUtils.getJwtAdminUser());
        Assert.assertNotNull(CGNUtils.getJwtAdminUserName());
        TestUtils.setOperatorAuth();
        Assert.assertNotNull(CGNUtils.getJwtOperatorUser());
        Assert.assertNotNull(CGNUtils.getJwtOperatorUserId());
        Assert.assertNotNull(CGNUtils.getJwtOperatorFiscalCode());
        Assert.assertNotNull(CGNUtils.getJwtOperatorFirstName());
        Assert.assertNotNull(CGNUtils.getJwtOperatorLastName());
    }

    @Test
    public void GetJwtOperatorUser_GetJwtOperatorUserWithAdminToken_CGNException() {
        TestUtils.setAdminAuth();
        Assert.assertThrows(CGNException.class, CGNUtils::getJwtOperatorUser);
    }

    @Test
    public void GetJwtAdminUser_GetJwtAdminUserWithUserToken_CGNException() {
        TestUtils.setOperatorAuth();
        Assert.assertThrows(CGNException.class, CGNUtils::getJwtAdminUserName);
    }

    @Test
    public void writeAttachmentsToStream_WritesToOutputStream()
            throws IOException {
        String fileName = "test.txt";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EmailParams.Attachment attachment = new EmailParams.Attachment(fileName, new ByteArrayResource(content));

        CGNUtils.writeAttachments(List.of(attachment), filename -> outputStream);

        Assert.assertArrayEquals(content, outputStream.toByteArray());
    }

}
