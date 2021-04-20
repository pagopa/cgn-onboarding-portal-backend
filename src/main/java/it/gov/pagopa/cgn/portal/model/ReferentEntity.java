package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "referent")
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
    @Column(name = "first_name", length = 50)
    private String firstName;

    @NotNull
    @NotBlank
    @Column(name = "last_name", length = 50)
    private String lastName;

    @NotNull
    @NotBlank
    @Email
    @Column(name = "email_address", length = 320)
    private String emailAddress;

    @NotNull
    @NotBlank
    @Column(name = "telephone_number", length = 15)
    private String telephoneNumber;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "referent")
    private ProfileEntity profile;

}