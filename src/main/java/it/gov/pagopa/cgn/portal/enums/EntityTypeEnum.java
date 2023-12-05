package it.gov.pagopa.cgn.portal.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntityTypeEnum {

    PRIVATE("PRIVATE", "Private"),
    PUBLIC_ADMINISTRATION("PUBLIC_ADMINISTRATION", "PublicAdministration");

    private final String code;
    private final String restRequestCode;

    public static EntityTypeEnum getByRestRequestCode(String restRequestCode) {
        for (EntityTypeEnum entityType : values()) {
            if (entityType.restRequestCode.equals(restRequestCode)) {
                return entityType;
            }
        }
        throw new IllegalArgumentException("No enum constant for restRequestCode: " + restRequestCode);
    }

}
