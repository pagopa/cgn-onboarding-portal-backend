package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.annotation.CheckProfile;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.util.ValidationUtils;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckProfileValidator implements ConstraintValidator<CheckProfile, ProfileEntity> {

    public boolean isValid(ProfileEntity profileEntity, ConstraintValidatorContext context) {
        return hasValidName(profileEntity) && hasValidAddresses(profileEntity);
    }

    private boolean hasValidAddresses(ProfileEntity profileEntity) {
        return profileEntity.getSalesChannel().equals(SalesChannelEnum.ONLINE) ||
               profileEntity.getAllNationalAddresses() ||
               !CollectionUtils.isEmpty(profileEntity.getAddressList());
    }

    private boolean hasValidName(ProfileEntity profileEntity) {
        return (ValidationUtils.isNullOrEmptyOrBlank(profileEntity.getName()) &&
                ValidationUtils.isNullOrEmptyOrBlank(profileEntity.getNameEn()) &&
                ValidationUtils.isNullOrEmptyOrBlank(profileEntity.getNameDe())) ||
               !(ValidationUtils.isNullOrEmptyOrBlank(profileEntity.getName()) ||
                 ValidationUtils.isNullOrEmptyOrBlank(profileEntity.getNameEn()) ||
                 ValidationUtils.isNullOrEmptyOrBlank(profileEntity.getNameDe()));
    }
}