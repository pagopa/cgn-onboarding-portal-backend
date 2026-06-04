package it.gov.pagopa.cgn.portal.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgreementStateEnum {

    DRAFT("DRAFT"),
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    TERMINATION_IN_PROGRESS("TERMINATION_IN_PROGRESS"),
    TERMINATED("TERMINATED");

    private final String code;
}