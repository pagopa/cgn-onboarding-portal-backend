package it.gov.pagopa.cgn.portal.exception;

public class ConflictErrorException extends RuntimeException {
    public ConflictErrorException(String message) {
        super(message);
    }
}
