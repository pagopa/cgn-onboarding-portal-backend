package it.gov.pagopa.cgn.portal.annotation;

 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.RetentionPolicy;
 import javax.validation.Constraint;
 import javax.validation.Payload;

import it.gov.pagopa.cgn.portal.validator.DateAfterValidator;

 @Documented
 @Target(ElementType.TYPE)
 @Retention(RetentionPolicy.RUNTIME)
 @Constraint(validatedBy = {DateAfterValidator.class})
 public @interface DateAfter {

     String message() default "Date must be after compareTo field date";

     Class<?>[] groups() default {};

     Class<? extends Payload>[] payload() default {};

     String target() default "";
     String compareTo() default "";
 }