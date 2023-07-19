package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "secondary_referent")
@Data
public class SecondaryReferentEntity extends ReferentEntity {

    @Id
    @Column(name = "referent_k")
    @SequenceGenerator(name="secondary_referent_referent_k_seq",
            sequenceName="secondary_referent_referent_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="secondary_referent_referent_k_seq")
    private Long id;


    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "profile_fk", nullable = false)
    private ProfileEntity profile;

}
