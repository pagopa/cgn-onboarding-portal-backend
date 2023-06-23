package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.annotation.CheckProfile;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.enums.SupportTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "profile")
@Data
@CheckProfile
public class ProfileEntity extends BaseEntity {

    @Id
    @Column(name = "profile_k")
    @SequenceGenerator(name = "profile_profile_k_seq", sequenceName = "profile_profile_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_profile_k_seq")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 100)
    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Size(max = 100)
    @Column(name = "name_de", length = 100)
    private String nameDe;

    @NotNull
    @NotBlank
    @Size(min = 8, max = 16)
    @Column(name = "tax_code_or_vat", length = 16)
    private String taxCodeOrVat;

    @NotNull
    @NotBlank
    @Email
    @Size(min = 5, max = 100)
    @Column(name = "pec_address", length = 100)
    private String pecAddress;

    @NotNull
    @NotBlank
    @Size(max = 300)
    @Column(name = "description", length = 300)
    private String description;

    @NotNull
    @NotBlank
    @Size(max = 300)
    @Column(name = "description_en", length = 300)
    private String descriptionEn;

    @NotNull
    @NotBlank
    @Size(max = 300)
    @Column(name = "description_de", length = 300)
    private String descriptionDe;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_channel", length = 50)
    @NotNull
    private SalesChannelEnum salesChannel;

    @Size(max = 500)
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "legal_office")
    private String legalOffice;

    @NotNull
    @NotBlank
    @Size(min = 4, max = 15)
    @Column(name = "telephone_number", length = 15)
    private String telephoneNumber;

    @NotNull
    @NotBlank
    @Size(max = 200)
    @Column(name = "legal_representative_full_name", length = 200)
    private String legalRepresentativeFullName;

    @NotNull
    @NotBlank
    @Size(min = 4, max = 20)
    @Column(name = "legal_representative_tax_code", length = 20)
    private String legalRepresentativeTaxCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_code_type", length = 20)
    private DiscountCodeTypeEnum discountCodeType;

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

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CCRecipientEntity> ccRecipientList;

    @Column(name = "all_national_addresses")
    private Boolean allNationalAddresses = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "support_type", length = 20)
    private SupportTypeEnum supportType;

    @NotNull
    @NotBlank
    @Size(min = 5, max = 500)
    @Column(name = "support_value", length = 500)
    private String supportValue;


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

    public OffsetDateTime getInsertTime() {
        return insertTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

}

