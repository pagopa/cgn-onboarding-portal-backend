package it.gov.pagopa.cgn.portal.security;

import lombok.Getter;

@Getter
public class ActiveDirectoryUser {
    private final String firstName;
    private final String lastName;

    public ActiveDirectoryUser(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
