package it.gov.pagopa.cgn.portal.wrapper;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import lombok.Getter;

public class CrudDiscountWrapper {
    @Getter
    private DiscountEntity discountEntity;

    @Getter
    private DiscountCodeTypeEnum profileDiscountCodeType;

    @Getter
    private boolean isChangedBucketLoad = true;

    public CrudDiscountWrapper(DiscountEntity discountEntity, DiscountCodeTypeEnum profileDiscountCodeType) {
        this.discountEntity = discountEntity;
        this.profileDiscountCodeType = profileDiscountCodeType;
    }

    public CrudDiscountWrapper(DiscountEntity discountEntity, DiscountCodeTypeEnum profileDiscountCodeType,
            boolean isChangedBucketLoad) {
        this.discountEntity = discountEntity;
        this.profileDiscountCodeType = profileDiscountCodeType;
        this.isChangedBucketLoad = isChangedBucketLoad;
    }

}
