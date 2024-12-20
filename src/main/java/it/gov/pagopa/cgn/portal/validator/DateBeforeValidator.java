package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.annotation.DateBefore;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateBeforeValidator
        implements ConstraintValidator<DateBefore, Object> {

    private String target;
    private String compareTo;

    @Override
    public void initialize(DateBefore constraintAnnotation) {
        this.target = constraintAnnotation.target();
        this.compareTo = constraintAnnotation.compareTo();
    }

    public boolean isValid(Object entity, ConstraintValidatorContext context) {

        LocalDate targetFieldValue = (LocalDate) new BeanWrapperImpl(entity).getPropertyValue(target);
        LocalDate compareToFieldValue = (LocalDate) new BeanWrapperImpl(entity).getPropertyValue(compareTo);

        if (targetFieldValue==null || compareToFieldValue==null) {
            return false;
        }
        return targetFieldValue.compareTo(compareToFieldValue) <= 0;
    }
}