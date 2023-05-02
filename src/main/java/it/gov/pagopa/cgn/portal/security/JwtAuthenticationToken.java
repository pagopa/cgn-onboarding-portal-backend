package it.gov.pagopa.cgn.portal.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtUser jwtUser;

    public JwtAuthenticationToken(JwtUser jwtUser) {
        super(Collections.singleton(jwtUser.getAuthority()));
        this.jwtUser = jwtUser;
        this.setAuthenticated(true);
    }

    //this return the same as getPrincipal and it's never used
    @Override
    public Object getCredentials() {
        return jwtUser;
    }

    @Override
    public Object getPrincipal() {
        return jwtUser;
    }
}
