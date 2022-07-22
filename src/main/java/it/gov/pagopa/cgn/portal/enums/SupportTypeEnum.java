package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SupportTypeEnum {

    WEBSITE("WEBSITE"), PHONENUMBER("PHONENUMBER"), EMAILADDRESS("EMAILADDRESS");

    private final String code;
}