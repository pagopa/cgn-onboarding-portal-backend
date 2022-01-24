package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BucketCodeExpiringThresholdEnum {

    PERCENT_0(0),
    PERCENT_10(10),
    PERCENT_25(25),
    PERCENT_50(50);

    private final int value;

}
