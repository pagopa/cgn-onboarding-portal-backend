package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountStateEnum {

    DRAFT("DRAFT"),
    PUBLISHED("PUBLISHED"),
    SUSPENDED("SUSPENDED"),
    TO_TEST("TO_TEST"),
    TEST_OK("TEST_OK"),
    TEST_KO("TEST_KO");

    private final String code;

}
