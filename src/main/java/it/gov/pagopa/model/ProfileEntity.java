package it.gov.pagopa.model;

import it.gov.pagopa.enums.SalesChannelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "PROFILE")
@SequenceGenerator(name = "PROFILE_SEQUENCE", sequenceName = "PROFILE_SEQ", allocationSize = 1)
@Data
public class ProfileEntity extends BaseEntity {

    @Id
    @Column(name = "PROFILE_K")
    @GeneratedValue(generator = "PROFILE_SEQUENCE")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "FULL_NAME", length = 100)
    private String fullName;

    @Column(name = "name", length = 100)
    private String name;

    @NotNull
    @NotBlank
    @Email
    @Column(name = "PEC_ADDRESS", length = 320)
    private String pecAddress;

    @NotNull
    @NotBlank
    @Column(name = "DESCRIPTION", length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "SALES_CHANNEL", length = 50)
    @NotNull
    private SalesChannelEnum salesChannel;

    @URL
    @Column(name = "WEBSITE_URL", length = 500)
    private String websiteUrl;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AGREEMENT_FK", updatable = false, nullable = false, unique = true)
    private AgreementEntity agreement;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "profile", optional = false, cascade = CascadeType.ALL)
    private ReferentEntity referent;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile", cascade = CascadeType.ALL)
    private List<AddressEntity> addressList;

}

