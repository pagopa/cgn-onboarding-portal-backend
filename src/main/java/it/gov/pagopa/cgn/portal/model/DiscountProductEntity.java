package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "discount_product_category")
@Data
@IdClass(DiscountProductKey.class)
@Convert(attributeName = "productCategory", converter = PostgreSQLEnumType.class)  // postgress enum type
public class DiscountProductEntity extends BaseEntity {

    @Id
    @Column(name = "product_category", length = 100)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ENUM)
    private ProductCategoryEnum productCategory;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk", nullable = false)
    private DiscountEntity discount;

}
