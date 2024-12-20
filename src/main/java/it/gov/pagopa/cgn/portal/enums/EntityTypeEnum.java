package it.gov.pagopa.cgn.portal.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntityTypeEnum {

    PRIVATE("PRIVATE", "Private", 2), PUBLIC_ADMINISTRATION("PUBLIC_ADMINISTRATION", "PublicAdministration", 1);

    private final String code;
    private final String restRequestCode;
    private final int nrDocs;

}
