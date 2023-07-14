package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "secondaryreferent")
@Data
public class SecondaryReferentEntity extends ReferentEntity {

    @Id
    @Column(name = "secondaryreferent_k")
    @SequenceGenerator(name="secondaryreferent_secondaryreferent_k_seq",
            sequenceName="secondaryreferent_secondaryreferent_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="secondaryreferent_secondaryreferent_k_seq")
    private Long id;


    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "profile_fk", nullable = false)
    private ProfileEntity profile;

}
