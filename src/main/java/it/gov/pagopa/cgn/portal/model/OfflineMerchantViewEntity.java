package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "offline_merchant")
@Data
public class OfflineMerchantViewEntity extends BaseMerchantViewEntity {

    @Column(name = "full_address")
    private String fullAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address_id")
    private Long addressId;
}
