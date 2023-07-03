package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ccrecipient")
@Data
public class CCRecipientEntity extends BaseEntity{

    public CCRecipientEntity(String emailAddress, ProfileEntity profile) {
        this.emailAddress = emailAddress;
        this.profile = profile;
    }

    @Id
    @Column(name = "ccrecipient_k")
    @SequenceGenerator(name="ccrecipient_ccrecipient_k_seq",
            sequenceName="ccrecipient_ccrecipient_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="ccrecipient_ccrecipient_k_seq")
    private Long id;


    @NotBlank
    @Email
    @Size(min = 5, max = 100)
    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "profile_fk", nullable = false)
    private ProfileEntity profile;




}
