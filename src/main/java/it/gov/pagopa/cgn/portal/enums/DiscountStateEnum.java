package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountStateEnum {

    DRAFT("DRAFT"),
    PUBLISHED("PUBLISHED"),
    SUSPENDED("SUSPENDED"),
    TEST_PENDING("TEST_PENDING"),
    TEST_PASSED("TEST_PASSED"),
    TEST_FAILED("TEST_FAILED");

    private final String code;

}
