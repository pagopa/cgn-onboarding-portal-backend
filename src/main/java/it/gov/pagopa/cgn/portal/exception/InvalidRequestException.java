package it.gov.pagopa.cgn.portal.exception;

public class InvalidRequestException
        extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}