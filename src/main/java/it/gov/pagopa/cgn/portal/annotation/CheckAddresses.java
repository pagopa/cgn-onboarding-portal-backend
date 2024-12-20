package it.gov.pagopa.cgn.portal.annotation;

import it.gov.pagopa.cgn.portal.validator.CheckAddressesValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CheckAddressesValidator.class})
public @interface CheckAddresses {

    String message() default "Address list is not compliant with selected SalesChannelMode or with allNationalAddresses field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
