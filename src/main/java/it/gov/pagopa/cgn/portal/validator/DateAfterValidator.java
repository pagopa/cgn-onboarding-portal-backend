package it.gov.pagopa.cgn.portal.validator;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;

import it.gov.pagopa.cgn.portal.annotation.DateAfter;

 public class DateAfterValidator implements ConstraintValidator<DateAfter, Object> {

    private String target;
    private String compareTo;

    public void initialize(DateAfter constraintAnnotation){
        this.target = constraintAnnotation.target();
        this.compareTo = constraintAnnotation.compareTo();
    }

     public boolean isValid(Object entity, ConstraintValidatorContext context) {

        LocalDate targetFieldValue = (LocalDate) new BeanWrapperImpl(entity)
          .getPropertyValue(target);
        LocalDate compareToFieldValue = (LocalDate) new BeanWrapperImpl(entity)
          .getPropertyValue(compareTo);
        return targetFieldValue.compareTo(compareToFieldValue) >= 0;
     }
 } 