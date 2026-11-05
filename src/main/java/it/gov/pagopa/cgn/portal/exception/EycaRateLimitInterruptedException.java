package it.gov.pagopa.cgn.portal.exception;

public class EycaRateLimitInterruptedException
        extends RuntimeException {

    public EycaRateLimitInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
