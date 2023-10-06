package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.annotation.CheckDiscount;
import it.gov.pagopa.cgn.portal.annotation.DateBefore;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "discount")
@Data
@TypeDef(name = "discount_state_enum", typeClass = PostgreSQLEnumType.class) // postgress enum type
@CheckDiscount
@DateBefore(target = "startDate", compareTo = "endDate", message = "Discount start date must be equal or before end date")
public class DiscountEntity extends BaseEntity {

    @Id
    @Column(name = "discount_k")
    @SequenceGenerator(name = "discount_discount_k_seq", sequenceName = "discount_discount_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_discount_k_seq")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type(type = "discount_state_enum")
    @Column(name = "state", length = 50)
    private DiscountStateEnum state;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name_en", length = 100)
    private String nameEn;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name_de", length = 100)
    private String nameDe;

    @Size(max = 250)
    @Column(name = "description", length = 250)
    private String description;

    @Size(max = 250)
    @Column(name = "description_en", length = 250)
    private String descriptionEn;

    @Size(max = 250)
    @Column(name = "description_de", length = 250)
    private String descriptionDe;

    @NotNull
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date")
    private LocalDate endDate;

    @Min(value = 1)
    @Max(value = 100)
    @Column(name = "discount_value")
    private Integer discountValue;

    @Size(max = 200)
    @Column(name = "condition", length = 200)
    private String condition;

    @Size(max = 200)
    @Column(name = "condition_en", length = 200)
    private String conditionEn;

    @Size(max = 200)
    @Column(name = "condition_de", length = 200)
    private String conditionDe;

    @Size(max = 100)
    @Column(name = "static_code", length = 100)
    private String staticCode;

    @Size(max = 500)
    @Column(name = "suspended_reason_message", length = 500)
    private String suspendedReasonMessage;

    @Column(name = "expiration_warning_sent")
    private OffsetDateTime expirationWarningSentDateTime;

    @Size(max = 500)
    @Column(name = "landing_page_url", length = 500)
    private String landingPageUrl;

    @Size(max = 100)
    @Column(name = "landing_page_referrer", length = 100)
    private String landingPageReferrer;

    @NotNull
    @Column(name = "visible_on_eyca")
    private Boolean visibleOnEyca = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_fk", updatable = false, nullable = false, unique = true)
    private AgreementEntity agreement;

    @NotNull
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min = 1)
    private List<DiscountProductEntity> products;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "last_bucket_code_load_id", referencedColumnName = "bucket_code_load_k")
    private BucketCodeLoadEntity lastBucketCodeLoad;

    @Size(max = 500)
    @Column(name = "discount_url", length = 500)
    private String discountUrl;

    @Size(max = 500)
    @Column(name = "test_failure_reason", length = 500)
    private String testFailureReason;

    @Size(max = 24)
    @Column(name = "eyca_update_id", length = 24)
    private String eycaUpdateId;


    @Transient
    private String lastBucketCodeLoadUid;

    @Transient
    private String lastBucketCodeLoadFileName;

    public void removeProduct(DiscountProductEntity productEntity) {
        this.products.remove(productEntity);
    }

    public void addProductList(Collection<DiscountProductEntity> productList) {
        if (!CollectionUtils.isEmpty(productList)) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            productList.forEach(p -> {
                if (!products.contains(p)) {
                    this.products.add(p);
                    p.setDiscount(this);
                }

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
