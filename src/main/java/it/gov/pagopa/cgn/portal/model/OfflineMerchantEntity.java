package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "offline_merchant")
@Data
public class OfflineMerchantEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

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

    @Column(name = "full_address")
    private String fullAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "last_update")
    private OffsetDateTime lastUpdate;
}
