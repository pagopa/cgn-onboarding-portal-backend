package it.gov.pagopa.cgn.portal.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class JwtOperatorUser
        implements JwtUser {

    private final String userFirstName;
    private final String userLastName;
    private final String userTaxCode;
    private final String companyTaxCode;

    public JwtOperatorUser(String userFirstName, String userLastName, String userTaxCode, String companyTaxCode) {
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userTaxCode = userTaxCode;
        this.companyTaxCode = companyTaxCode;
    }

    @Override
    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(CgnUserRoleEnum.OPERATOR.getCode());
    }
}
