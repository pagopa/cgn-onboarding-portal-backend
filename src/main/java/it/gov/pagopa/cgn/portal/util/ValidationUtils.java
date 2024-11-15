package it.gov.pagopa.cgn.portal.util;

import io.jsonwebtoken.lang.Collections;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ValidationUtils {

    public static void performConstraintValidation(Validator validator, Object entity) {
        log.info(entity.toString());
        Set<ConstraintViolation<Object>> validationResults = validator.validate(entity);
        if (!Collections.isEmpty(validationResults)) {
            throw new InvalidRequestException(validationResults.stream()
                                                               .map(e -> e.getPropertyPath().toString() +
                                                                         "|" +
                                                                         e.getMessage())
                                                               .collect(Collectors.joining("\n")));
        }
    }

    public static boolean isNullOrEmptyOrBlank(String s) {
        return s == null || s.isEmpty() || s.isBlank();
    }

}
