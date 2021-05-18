package it.gov.pagopa.cgn.portal.model;

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
@TypeDef(name = "discount_state_enum", typeClass = PostgreSQLEnumType.class)  // postgress enum type
public class DiscountEntity extends BaseEntity {

    @Id
    @Column(name = "discount_k")
    @SequenceGenerator(name="discount_discount_k_seq",
            sequenceName="discount_discount_k_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="discount_discount_k_seq")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type( type = "discount_state_enum" )
    @Column(name = "state", length = 50)
    private DiscountStateEnum state;

    @NotNull
    @NotBlank
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 250)
    private String description;

    @NotNull
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    @Column(name = "discount_value")
    private Integer discountValue;

    @Column(name = "condition", length = 200)
    private String condition;

    @Column(name = "static_code", length = 100)
    private String staticCode;

    @Column(name = "suspended_reason_message", length = 250)
    private String suspendedReasonMessage;

    @Column(name = "expiration_warning_sent")
    private OffsetDateTime expirationWarningSentDateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_fk", updatable = false, nullable = false, unique = true)
    private AgreementEntity agreement;

    @NotNull
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min = 1)
    private List<DiscountProductEntity> products;

    public void removeProduct(DiscountProductEntity productEntity) {
        this.products.remove(productEntity);
    }

    public void addProductList(Collection<DiscountProductEntity> productList) {
        if (!CollectionUtils.isEmpty(productList)) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            productList.forEach(p-> {
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
