package it.gov.pagopa.cgn.portal.model;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class DiscountProductKey implements Serializable {

    private String productCategory;

    private DiscountEntity discount;

}
