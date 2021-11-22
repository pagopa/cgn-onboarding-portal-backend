package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BucketCodeLoadStatusEnum {

    PENDING("PENDING"), RUNNING("RUNNING"), FINISHED("FINISHED"), FAILED("FAILED");

    private final String code;

}
