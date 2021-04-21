package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "profile")
@Data
public class ProfileEntity extends BaseEntity {

    @Id
    @Column(name = "profile_k")
    @SequenceGenerator(name="profile_profile_k_seq",
            sequenceName="profile_profile_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="profile_profile_k_seq")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "name", length = 100)
    private String name;

    @NotNull
    @NotBlank
    @Email
    @Column(name = "pec_address", length = 320)
    private String pecAddress;

    @NotNull
    @NotBlank
    @Column(name = "description", length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_channel", length = 50)
    @NotNull
    private SalesChannelEnum salesChannel;

    @URL
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_fk", updatable = false, nullable = false, unique = true)
    private AgreementEntity agreement;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "referent_fk", nullable = false)
    private ReferentEntity referent;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addressList;

    public void removeAllAddress() {
        this.addressList.clear();
    }

    public void addAddressList(Collection<AddressEntity> addresses) {
        if (!CollectionUtils.isEmpty(addresses)) {
            if (this.addressList == null) {
                this.addressList = new ArrayList<>();
            }
            addresses.forEach(a -> {
                addressList.add(a);
                a.setProfile(this);
            });
        }
    }

}

