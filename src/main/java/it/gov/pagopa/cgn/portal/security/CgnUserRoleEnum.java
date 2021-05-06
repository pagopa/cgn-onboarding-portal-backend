package it.gov.pagopa.cgn.portal.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CgnUserRoleEnum {

    OPERATOR("ROLE_MERCHANT"), ADMIN("ROLE_ADMIN");

    private final String code;

}
