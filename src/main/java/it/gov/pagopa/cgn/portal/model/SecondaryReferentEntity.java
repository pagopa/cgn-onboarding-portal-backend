package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "secondary_referent")
@Data
public class SecondaryReferentEntity
        extends BaseEntity {

    @Id
    @Column(name = "referent_k")
    @SequenceGenerator(name = "secondary_referent_referent_k_seq",
                       sequenceName = "secondary_referent_referent_k_seq",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "secondary_referent_referent_k_seq")
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
    @ManyToOne
    @JoinColumn(name = "profile_fk", nullable = false)
    private ProfileEntity profile;

    public SecondaryReferentEntity() {
    }


}
