package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class DiscountProductKey
        implements Serializable {

    private ProductCategoryEnum productCategory;

    private DiscountEntity discount;

}
