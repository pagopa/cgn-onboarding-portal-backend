package it.gov.pagopa.cgn.portal.security;

import lombok.Getter;

@Getter
public class OneIdentityUser {
    private final String fiscalCode;
    private final String firstName;
    private final String lastName;

    public OneIdentityUser(String fiscalCode, String firstName, String lastName) {
        this.fiscalCode = fiscalCode;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
