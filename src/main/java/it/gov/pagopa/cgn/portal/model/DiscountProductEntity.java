package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "discount_product_category")
@Data
@IdClass(DiscountProductKey.class)
public class DiscountProductEntity extends BaseEntity {

    @Id
    @Column(name = "product_category", length = 100)
    private String productCategory;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk", nullable = false)
    private DiscountEntity discount;

}
