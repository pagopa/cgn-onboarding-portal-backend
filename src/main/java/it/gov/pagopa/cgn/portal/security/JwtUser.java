package it.gov.pagopa.cgn.portal.security;

import org.springframework.security.core.GrantedAuthority;

public interface JwtUser {

    GrantedAuthority getAuthority();

}
