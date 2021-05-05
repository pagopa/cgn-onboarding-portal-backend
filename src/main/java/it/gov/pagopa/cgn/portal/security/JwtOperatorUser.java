package it.gov.pagopa.cgn.portal.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class JwtOperatorUser implements JwtUser {

    private final String userTaxCode;
    private final String merchantTaxCode;
    private final String merchantLegalName;

    public JwtOperatorUser(
            String userTaxCode,
            String merchantTaxCode,
            String merchantLegalName
    ) {
        this.userTaxCode = userTaxCode;
        this.merchantTaxCode = merchantTaxCode;
        this.merchantLegalName = merchantLegalName;
    }

    @Override
    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(CgnUserRoleEnum.OPERATOR.getCode());
    }
}
