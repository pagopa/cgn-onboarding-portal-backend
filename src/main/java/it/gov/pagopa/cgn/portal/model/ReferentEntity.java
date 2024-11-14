package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "referent")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
public class ReferentEntity extends BaseEntity {

    @Id
    @Column(name = "referent_k")
    @SequenceGenerator(name="referent_referent_k_seq",
            sequenceName="referent_referent_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="referent_referent_k_seq")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    private String firstName;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    private String lastName;

    @NotNull
    @NotBlank
    @Email
    @Size(min = 5, max = 100)
    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @NotNull
    @NotBlank
    @Size(min = 4, max = 15)
    @Column(name = "telephone_number", length = 15)
    private String telephoneNumber;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "role", length = 100)
    private String role;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "referent")
    private ProfileEntity profile;

}