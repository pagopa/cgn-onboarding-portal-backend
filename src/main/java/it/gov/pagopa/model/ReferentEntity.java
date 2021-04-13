package it.gov.pagopa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "REFERENT")
@SequenceGenerator(name = "REFERENT_SEQUENCE", sequenceName = "REFERENT_SEQ", allocationSize = 1)
@Data
public class ReferentEntity extends BaseEntity {

    @Id
    @Column(name = "REFERENT_K")
    @GeneratedValue(generator = "REFERENT_SEQUENCE")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "FIRST_NAME", length = 50)
    private String firstName;

    @NotNull
    @NotBlank
    @Column(name = "LAST_NAME", length = 50)
    private String lastName;

    @NotNull
    @NotBlank
    @Email
    @Column(name = "EMAIL_ADDRESS", length = 320)
    private String emailAddress;

    @NotNull
    @NotBlank
    @Column(name = "TELEPHONE_NUMBER", length = 15)
    private String telephoneNumber;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFILE_FK", nullable = false)
    private ProfileEntity profile;

}