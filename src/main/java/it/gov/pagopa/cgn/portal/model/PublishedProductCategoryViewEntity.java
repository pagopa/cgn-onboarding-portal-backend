package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import lombok.Data;
import org.hibernate.annotations.Immutable;


import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Immutable
@Table(name = "published_product_category")
public class PublishedProductCategoryViewEntity {

    @Id
    @Column(name = "product_category")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)    private ProductCategoryEnum productCategory;

    @Column(name = "new_discounts")
    private Integer newDiscounts;

}

