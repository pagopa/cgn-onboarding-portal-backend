package it.gov.pagopa.config;

import it.gov.pagopa.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class AppExceptionHandler {

    @Autowired
    private ConfigProperties configProperties;

    @ExceptionHandler(value = {InvalidRequestException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleInvalidRequestException(Exception ex) {
        log.error("InvalidRequestException", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        String errorMsg = configProperties.isActiveProfileDev() ?
                ex.getMessage(): configProperties.getExceptionGenericMessage();
        log.error("Uncaught Exception", ex);
        return new ResponseEntity<>(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}