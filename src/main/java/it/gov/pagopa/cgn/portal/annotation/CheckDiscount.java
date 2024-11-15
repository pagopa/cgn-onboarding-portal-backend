package it.gov.pagopa.cgn.portal.annotation;

import it.gov.pagopa.cgn.portal.validator.CheckDiscountValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CheckDiscountValidator.class})
public @interface CheckDiscount {

    String message() default "Cannot save Discount because it is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
