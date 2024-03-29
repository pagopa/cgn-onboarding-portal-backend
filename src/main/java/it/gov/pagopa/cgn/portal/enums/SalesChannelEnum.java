package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SalesChannelEnum {

    ONLINE("ONLINE"), OFFLINE("OFFLINE"), BOTH("BOTH");

    private final String code;
}