package it.gov.pagopa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountStateEnum {

    DRAFT("DRAFT"), PUBLISHED("PUBLISHED"), REJECTED("REJECTED");

    private final String code;

}
