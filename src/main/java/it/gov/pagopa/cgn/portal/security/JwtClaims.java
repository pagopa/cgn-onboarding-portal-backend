package it.gov.pagopa.cgn.portal.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JwtClaims {
    ROLE("role"),
    FIRST_NAME("first_name"),
    LAST_NAME("last_name"),
    FISCAL_CODE("fiscal_code"),
    AGREEMENT("agreement"),
    ORGANIZATION_FISCAL_CODE("organization_fiscal_code");

    private final String code;
}
