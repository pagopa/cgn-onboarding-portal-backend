package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;

public class CGNUtils {

    private CGNUtils() {}

    public static LocalDate getDefaultAgreementEndDate() {
        return LocalDate.now().plusYears(1);
    }

    public static void validateImage(MultipartFile image, int minWidth, int minHeight) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(image.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean isValid = minWidth <= bufferedImage.getWidth() && minHeight <= bufferedImage.getHeight();
        if (!isValid) {
            throw new InvalidRequestException("Image must be at least " + minHeight + "x" + minWidth);
        }
    }

    public static void checkIfPdfFile(String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith("pdf")) {
            throw new InvalidRequestException("Invalid file extension. Upload a PDF document.");
        }
    }

    public static void setSecureDocumentUrl(DocumentEntity documentEntity, AzureStorage azureStorage) {
        documentEntity.setDocumentUrl(azureStorage.getDocumentSasFileUrl(documentEntity.getDocumentUrl()));
    }

    public static String getJwtOperatorUserId() {
        return getJwtOperatorUser().getUserTaxCode();
    }

    public static String getJwtAdminUserName() {
        return getJwtAdminUser().getUserFullName();
    }

    public static JwtOperatorUser getJwtOperatorUser() {
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (token.getPrincipal() instanceof JwtOperatorUser) {
            return (JwtOperatorUser) token.getPrincipal();
        }
       throw new RuntimeException("Expected an operator token, but was of type " + token.getPrincipal());
    }

    public static JwtAdminUser getJwtAdminUser() {
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (token.getPrincipal() instanceof JwtAdminUser) {
            return (JwtAdminUser) token.getPrincipal();
        }
        throw new RuntimeException("Expected an admin token, but was of type " + token.getPrincipal());
    }

}
