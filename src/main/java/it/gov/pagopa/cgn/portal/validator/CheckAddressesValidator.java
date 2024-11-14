package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.annotation.CheckAddresses;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.springframework.util.CollectionUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckAddressesValidator implements ConstraintValidator<CheckAddresses, ProfileEntity> {

    public boolean isValid(ProfileEntity profileEntity, ConstraintValidatorContext context) {
        return profileEntity.getSalesChannel().equals(SalesChannelEnum.ONLINE) ||
               profileEntity.getAllNationalAddresses() ||
               !CollectionUtils.isEmpty(profileEntity.getAddressList());
    }
}