package it.gov.pagopa.cgn.portal.annotation;

import it.gov.pagopa.cgn.portal.validator.DateBeforeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DateBeforeValidator.class})
public @interface DateBefore {

    String message() default "Date must be before compareTo field date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String target() default "";

    String compareTo() default "";
}