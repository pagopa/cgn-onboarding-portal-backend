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
        INVALID_IMAGE_TYPE("Invalid file extension. Upload a JPG or PNG image."),
        INVALID_DIMENSION("Invalid image format."),
        GENERIC("Unexpected error");

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
            case INVALID_IMAGE_TYPE:
                return ImageErrorCode.INVALID_IMAGE_TYPE.getValue();
            case INVALID_DIMENSION:
                return ImageErrorCode.INVALID_DIMENSION.getValue();
            default:
                return ImageErrorCode.GENERIC.getValue();
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
