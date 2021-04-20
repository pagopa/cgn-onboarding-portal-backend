package it.gov.pagopa.model;

import it.gov.pagopa.enums.DiscountStateEnum;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "DISCOUNT")
@SequenceGenerator(name = "DISCOUNT_SEQUENCE", sequenceName = "DISCOUNT_SEQ", allocationSize = 1)
@Data
public class DiscountEntity extends BaseEntity {

    @Id
    @Column(name = "DISCOUNT_K")
    @GeneratedValue(generator = "DISCOUNT_SEQUENCE")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", length = 50)
    private DiscountStateEnum state;

    @NotNull
    @NotBlank
    @Column(name = "NAME", length = 100)
    private String name;

    @NotNull
    @NotBlank
    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @NotNull
    @Column(name = "START_DATE")
    private LocalDate startDate;

    @NotNull
    @Column(name = "END_DATE")
    private LocalDate endDate;

    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    @Column(name = "DISCOUNT_VALUE")
    private Double discountValue;

    @NotNull
    @NotBlank
    @Column(name = "CONDITION", length = 200)
    private String condition;

    @Column(name = "STATIC_CODE", length = 100)
    private String staticCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AGREEMENT_FK", updatable = false, nullable = false, unique = true)
    private AgreementEntity agreement;

    @NotNull
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min = 1)
    private List<DiscountProductEntity> products;

    public void removeAllProduct() {
        this.products.clear();
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
}
