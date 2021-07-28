package it.gov.pagopa.cgn.portal.util;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;

import io.jsonwebtoken.lang.Collections;

public class ValidationUtils {

    public static void performConstraintValidation(Validator validator, Object entity){
        Set<ConstraintViolation<Object>> validationResults = validator.validate(entity);
        if (!Collections.isEmpty(validationResults)){
            throw new InvalidRequestException(
                validationResults.stream()
                .map(e -> e.getPropertyPath().toString() + "|" + e.getMessage())
                .collect(Collectors.joining("\n"))
            );
        }
        return;
    }
    
}
