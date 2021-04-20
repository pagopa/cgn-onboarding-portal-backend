package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "address")
@Data
public class AddressEntity extends BaseEntity {

    @Id
    @Column(name = "address_k")
    @SequenceGenerator(name="address_address_k_seq",
            sequenceName="address_address_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="address_address_k_seq")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "street")
    private String street;

    @NotNull
    @NotBlank
    @Column(name = "zip_code", length = 5)
    private String zipCode;

    @NotNull
    @NotBlank
    @Column(name = "city")
    private String city;

    @NotNull
    @NotBlank
    @Column(name = "district", length = 2)
    private String district;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "profile_fk", nullable = false)
    private ProfileEntity profile;

}

