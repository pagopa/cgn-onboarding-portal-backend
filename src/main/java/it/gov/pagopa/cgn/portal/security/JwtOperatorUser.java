package it.gov.pagopa.cgn.portal.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class JwtOperatorUser implements JwtUser {

    private final String userTaxCode;
    private final String merchantTaxCode;

    public JwtOperatorUser(
            String userTaxCode,
            String merchantTaxCode
    ) {
        this.userTaxCode = userTaxCode;
        this.merchantTaxCode = merchantTaxCode;
    }

    @Override
    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(CgnUserRoleEnum.OPERATOR.getCode());
    }
}
