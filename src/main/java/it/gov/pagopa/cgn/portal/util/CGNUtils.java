package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.email.EmailParams.Attachment;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.ImageException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.security.JwtAdminUser;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;

import org.apache.commons.io.FileUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.*;

public class CGNUtils {

    private CGNUtils() {
    }

    public static LocalDate getDefaultAgreementEndDate() {
        return LocalDate.now().plusYears(1);
    }

    public static void validateImage(MultipartFile image, int minWidth, int minHeight) {
        Dimension dimension;
        try {
            checkIfImageFile(image.getOriginalFilename());
            dimension = getImageDimensions(image.getInputStream());
        } catch (IOException e) {
            throw new ImageException(ImageException.ImageErrorCodeEnum.GENERIC);
        }
        boolean isValid = minWidth <= dimension.getWidth() && minHeight <= dimension.getHeight();
        if (!isValid) {
            throw new ImageException(ImageException.ImageErrorCodeEnum.INVALID_DIMENSION,
                    "Image must be at least " + minWidth + "x" + minHeight);
        }
    }

    private static Dimension getImageDimensions(Object input) throws IOException {

        try (ImageInputStream stream = ImageIO.createImageInputStream(input)) { // accepts File, InputStream,
                                                                                // RandomAccessFile
            if (stream != null) {
                IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
                Iterator<ImageReaderSpi> iter = iioRegistry.getServiceProviders(ImageReaderSpi.class, true);
                while (iter.hasNext()) {
                    ImageReaderSpi readerSpi = iter.next();
                    if (readerSpi.canDecodeInput(stream)) {
                        ImageReader reader = readerSpi.createReaderInstance();
                        try {
                            reader.setInput(stream);
                            int width = reader.getWidth(reader.getMinIndex());
                            int height = reader.getHeight(reader.getMinIndex());
                            return new Dimension(width, height);
                        } finally {
                            reader.dispose();
                        }
                    }
                }
            }
            throw new IllegalArgumentException("Can't get dimensions for this image");
        }
    }

    public static void checkIfPdfFile(String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith("pdf")) {
            throw new InvalidRequestException("Invalid file extension. Upload a PDF document.");
        }
    }

    public static void checkIfCsvFile(String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith("csv")) {
            throw new InvalidRequestException("Invalid file extension. Upload a CSV document.");
        }
    }

    public static void checkIfImageFile(String fileName) {
        if (fileName == null || !(fileName.toLowerCase().endsWith("jpg") || fileName.toLowerCase().endsWith("png"))) {
            throw new ImageException(ImageException.ImageErrorCodeEnum.INVALID_IMAGE_TYPE);
        }
    }

    public static String getJwtOperatorUserId() {
        return getJwtOperatorUser().getCompanyTaxCode();
    }

    public static String getJwtAdminUserName() {
        return getJwtAdminUser().getUserFullName();
    }

    public static JwtOperatorUser getJwtOperatorUser() {
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (token.getPrincipal() instanceof JwtOperatorUser) {
            return (JwtOperatorUser) token.getPrincipal();
        }
        throw new CGNException("Expected an operator token, but was of type " + token.getPrincipal());
    }

    public static JwtAdminUser getJwtAdminUser() {
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (token.getPrincipal() instanceof JwtAdminUser) {
            return (JwtAdminUser) token.getPrincipal();
        }
        throw new CGNException("Expected an admin token, but was of type " + token.getPrincipal());
    }
    
    public static String toJson (Object o) {
          try {  
              return new ObjectMapper().writer().writeValueAsString(o);  
          }  
          catch (Exception e) {
        	  return "null";
          }  
    }
    
	public static void writeAttachments(List<Attachment> attachments, String path) throws IOException {
		for(Attachment a : attachments) {
			FileUtils.writeByteArrayToFile(new File(path+a.getAttachmentFilename()), a.getResource().getByteArray());
		}
	}

}
