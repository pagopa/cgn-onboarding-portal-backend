package it.gov.pagopa.cgn.portal.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import it.gov.pagopa.cgn.portal.validator.CheckAddressesValidator;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CheckAddressesValidator.class})
public @interface CheckAddresses {

    String message() default "Address list is not compliant with selected SalesChannelMode or with allNationalAddresses field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
