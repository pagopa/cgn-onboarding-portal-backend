package it.gov.pagopa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ADDRESS")
@SequenceGenerator(name = "ADDRESS_SEQUENCE", sequenceName = "ADDRESS_SEQ", allocationSize = 1)
@Data
public class AddressEntity extends BaseEntity {

    @Id
    @Column(name = "ADDRESS_K")
    @GeneratedValue(generator = "ADDRESS_SEQUENCE")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "STREET")
    private String street;

    @NotNull
    @NotBlank
    @Column(name = "ZIP_CODE", length = 5)
    private String zipCode;

    @NotNull
    @NotBlank
    @Column(name = "CITY")
    private String city;

    @NotNull
    @NotBlank
    @Column(name = "DISTRICT", length = 2)
    private String district;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Column(name = "LONGITUDE")
    private Double longitude;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "PROFILE_FK", nullable = false)
    private ProfileEntity profile;

}

