package it.gov.pagopa.cgn.portal.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CgnUserRoleEnum {

    OPERATOR("merchant"), ADMIN("admin");

    private final String code;

}
