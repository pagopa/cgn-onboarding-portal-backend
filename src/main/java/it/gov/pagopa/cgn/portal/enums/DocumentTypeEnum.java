package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {

    AGREEMENT("AGREEMENT"),
    MANIFESTATION_OF_INTEREST("MANIFESTATION_OF_INTEREST");

    private final String code;
}
