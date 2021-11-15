package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountCodeTypeEnum {

    STATIC("STATIC"), API("API"), LANDINGPAGE("LANDINGPAGE"), BUCKET("BUCKET");

    private final String code;

}
