package it.gov.pagopa.cgn.portal.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class JwtAdminUser implements JwtUser {

    private final String userTaxCode;
    private final String userFullName;

    public JwtAdminUser(
            String userTaxCode,
            String userFullName
    ) {
        this.userTaxCode = userTaxCode;
        this.userFullName = userFullName;
    }

    @Override
    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(CgnUserRoleEnum.ADMIN.getCode());
    }
}
