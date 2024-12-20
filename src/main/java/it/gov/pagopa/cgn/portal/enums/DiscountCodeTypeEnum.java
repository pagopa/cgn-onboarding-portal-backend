package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountCodeTypeEnum {

    STATIC("STATIC", "STATIC CODE"),
    API("API", "API"),
    LANDINGPAGE("LANDINGPAGE", "LANDING PAGE"),
    BUCKET("BUCKET", "LIST OF STATIC CODES");

    private final String code;
    private final String eycaDataCode;

}
