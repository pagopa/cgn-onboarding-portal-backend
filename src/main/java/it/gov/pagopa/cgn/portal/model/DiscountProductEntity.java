package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(name = "discount_product_category")
@Data
@IdClass(DiscountProductKey.class)
@TypeDef(name = "product_category_enum", typeClass = PostgreSQLEnumType.class)  // postgress enum type
public class DiscountProductEntity
        extends BaseEntity {

    @Id
    @Column(name = "product_category", length = 100)
    @Enumerated(EnumType.STRING)
    @Type(type = "product_category_enum")
    private ProductCategoryEnum productCategory;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk", nullable = false)
    private DiscountEntity discount;

}
