package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.annotation.CheckDiscount;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.util.ValidationUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckDiscountValidator implements ConstraintValidator<CheckDiscount, DiscountEntity> {

    public boolean isValid(DiscountEntity discountEntity, ConstraintValidatorContext context) {
        return hasValidConditions(discountEntity) && hasValidDescriptions(discountEntity);
    }

    private boolean hasValidDescriptions(DiscountEntity discountEntity) {
        return // all empty
                (ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getDescription()) &&
                 ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getDescriptionEn()) &&
                 ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getDescriptionDe())) ||
                // all full
                !(ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getDescription()) ||
                  ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getDescriptionEn()) ||
                  ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getDescriptionDe()));
    }

    private boolean hasValidConditions(DiscountEntity discountEntity) {
        return // all empty
                (ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getCondition()) &&
                 ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getConditionEn()) &&
                 ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getConditionDe())) ||
                // all full
                !(ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getCondition()) ||
                  ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getConditionEn()) ||
                  ValidationUtils.isNullOrEmptyOrBlank(discountEntity.getConditionDe()));
    }

}