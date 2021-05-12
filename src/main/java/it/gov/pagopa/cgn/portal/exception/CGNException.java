package it.gov.pagopa.cgn.portal.exception;

public class CGNException extends RuntimeException {

    public CGNException(Throwable cause) {
        super(cause);
    }

    public CGNException(String message) {
        super(message);
    }

    public CGNException(String message, Throwable cause) {
        super(message, cause);
    }
}
