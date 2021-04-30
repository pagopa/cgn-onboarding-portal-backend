package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
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
}
