package it.gov.pagopa.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "DISCOUNT_PRODUCT_CATEGORY")
@Data
public class DiscountProductEntity extends BaseEntity {

    @Id
    @Column(name = "PRODUCT_CATEGORY", length = 100)
    private String productCategory;

    @ToString.Exclude
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISCOUNT_FK", nullable = false)
    private DiscountEntity discount;

}
