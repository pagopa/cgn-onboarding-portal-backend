package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "online_merchant")
@Data
public class OnlineMerchantEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "website_url")
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_code_type")
    private DiscountCodeTypeEnum discountCodeType;


    @Column(name = "product_categories", columnDefinition = "text[]")
    @Type(type = "it.gov.pagopa.cgn.portal.type.ProductCategoryArrayType")
    private ProductCategoryEnum[] productCategories;

    @Column(name = "searchable_name")
    private String searchableName;

    @Column(name = "banking_services")
    private boolean bankingServices;

    @Column(name = "culture_and_entertainment")
    private boolean cultureAndEntertainment;

    @Column(name = "health")
    private boolean health;

    @Column(name = "home")
    private boolean home;

    @Column(name = "job_offers")
    private boolean jobOffers;

    @Column(name = "learning")
    private boolean learning;

    @Column(name = "sports")
    private boolean sports;

    @Column(name = "sustainable_mobility")
    private boolean sustainableMobility;

    @Column(name = "telephony_and_internet")
    private boolean telephonyAndInternet;

    @Column(name = "travelling")
    private boolean travelling;

    @Column(name = "last_update")
    private OffsetDateTime lastUpdate;
}
