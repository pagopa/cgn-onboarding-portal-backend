package it.gov.pagopa.cgn.portal.wrapper;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import lombok.Getter;

public class CreateDiscountWrapper {
    @Getter
    private DiscountEntity discountEntity;

    @Getter
    private DiscountCodeTypeEnum profileDiscountCodeType;

    public CreateDiscountWrapper(DiscountEntity discountEntity, DiscountCodeTypeEnum profileDiscountCodeType) {
        this.discountEntity = discountEntity;
        this.profileDiscountCodeType = profileDiscountCodeType;
    }

}
