package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Data
@Entity
@Immutable
@Table(name = "published_product_category")
public class PublishedProductCategoryViewEntity {

    @Id
    @Column(name = "product_category")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private ProductCategoryEnum productCategory;

    @Column(name = "new_discounts")
    private Integer newDiscounts;

}

