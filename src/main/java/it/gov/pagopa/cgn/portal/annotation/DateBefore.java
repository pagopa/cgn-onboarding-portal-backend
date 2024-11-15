package it.gov.pagopa.cgn.portal.annotation;

 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.RetentionPolicy;
 import jakarta.validation.Constraint;
 import jakarta.validation.Payload;
import it.gov.pagopa.cgn.portal.validator.DateBeforeValidator;

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