package it.gov.pagopa.cgn.portal.enums;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {


    AGREEMENT("AGREEMENT", Type.AGREEMENT,false),
    ADHESION_REQUEST("ADHESION_REQUEST", Type.ADHESION_REQUEST, false),
    BACKOFFICE_AGREEMENT("BACKOFFICE_AGREEMENT", Type.AGREEMENT, true),
    BACKOFFICE_ADHESION_REQUEST("BACKOFFICE_ADHESION_REQUEST", Type.ADHESION_REQUEST,
            true);

    private final String code;
    private final Type type;
    private final boolean backoffice;

    public enum Type {
        AGREEMENT, ADHESION_REQUEST
    }

    public static int getNumberOfDocumentProfile() {
        return (int) Arrays.stream(DocumentTypeEnum.values())
                .filter(d -> !d.isBackoffice()).count();
    }

    public static DocumentTypeEnum fromValue(String value) {
        return Arrays.stream(DocumentTypeEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(()-> new InvalidRequestException(ErrorCodeEnum.DOCUMENT_TYPE_NOT_VALID.getValue()));
    }

}
