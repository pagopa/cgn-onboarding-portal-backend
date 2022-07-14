package it.gov.pagopa.cgn.portal.annotation;

import it.gov.pagopa.cgn.portal.validator.CheckProfileValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CheckProfileValidator.class})
public @interface CheckProfile {

    String message() default "Cannot save Profile because it is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
