package it.gov.pagopa.cgn.portal.enums;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {


    AGREEMENT("AGREEMENT", Type.AGREEMENT,false),
    MANIFESTATION_OF_INTEREST("MANIFESTATION_OF_INTEREST", Type.MANIFESTATION_OF_INTEREST, false),
    BACKOFFICE_AGREEMENT("BACKOFFICE_AGREEMENT", Type.AGREEMENT, true),
    BACKOFFICE_MANIFESTATION_OF_INTEREST("BACKOFFICE_MANIFESTATION_OF_INTEREST", Type.MANIFESTATION_OF_INTEREST,
            true);

    private final String code;
    private final Type type;
    private final boolean backoffice;

    public enum Type {
        AGREEMENT, MANIFESTATION_OF_INTEREST
    }

    public static int getNumberOfDocumentProfile() {
        return (int) Arrays.stream(DocumentTypeEnum.values())
                .filter(d -> !d.isBackoffice()).count();
    }

    public static DocumentTypeEnum fromValue(String value) {
        return Arrays.stream(DocumentTypeEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equals(value)).findFirst()
                .orElseThrow(()-> new InvalidRequestException("DocumentTypeEnum value not valid"));
    }

}
