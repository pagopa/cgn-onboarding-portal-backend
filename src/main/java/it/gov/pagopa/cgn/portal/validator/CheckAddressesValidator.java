package it.gov.pagopa.cgn.portal.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.CollectionUtils;

import it.gov.pagopa.cgn.portal.annotation.CheckAddresses;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;

public class CheckAddressesValidator implements ConstraintValidator<CheckAddresses, ProfileEntity> {

    public boolean isValid(ProfileEntity profileEntity, ConstraintValidatorContext context) {
        return profileEntity.getSalesChannel().equals(SalesChannelEnum.ONLINE)
                || ((CollectionUtils.isEmpty(profileEntity.getAddressList()) && profileEntity.getAllNationalAddresses()
                        || !CollectionUtils.isEmpty(profileEntity.getAddressList())
                                && !profileEntity.getAllNationalAddresses())
                        && !profileEntity.getSalesChannel().equals(SalesChannelEnum.ONLINE));
    }
}