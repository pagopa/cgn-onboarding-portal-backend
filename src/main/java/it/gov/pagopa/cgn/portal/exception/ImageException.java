package it.gov.pagopa.cgn.portal.exception;

import it.gov.pagopa.cgnonboardingportal.model.ImageErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
public class ImageException extends RuntimeException {

    @AllArgsConstructor
    @Getter
    public enum ImageErrorCodeEnum {
        INVALID_IMAGE("INVALID_IMAGE", "Invalid file extension. Upload a JPG or PNG image."),
        INVALID_FORMAT("INVALID_FORMAT", "Invalid image format."),
        GENERIC("GENERIC", "Unexpected error");
        private final String code;
        private final String defaultErrorMsg;
    }

    private final ImageErrorCodeEnum imageErrorCodeEnum;

    private final String message;

    public ImageException(ImageErrorCodeEnum imageErrorCodeEnum) {
        this.imageErrorCodeEnum = imageErrorCodeEnum;
        this.message = imageErrorCodeEnum.defaultErrorMsg;
    }
    public ImageException(ImageErrorCodeEnum imageErrorCodeEnum, String errorMessage) {
        this.imageErrorCodeEnum = imageErrorCodeEnum;
        this.message = errorMessage;
    }

    public String getImageErrorCodeDto() {
        switch (imageErrorCodeEnum) {
            case INVALID_IMAGE:
                return ImageErrorCode.INVALID_IMAGE.getValue();
            case INVALID_FORMAT:
                return ImageErrorCode.INVALID_FORMAT.getValue();
            case GENERIC:
                return ImageErrorCode.GENERIC.getValue();
            default:
                return "Image Error";
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
