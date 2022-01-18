package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "discount_bucket_code_summary")
@Data
public class DiscountBucketCodeSummaryEntity implements Serializable {

    @Id
    @Column(name = "discount_bucket_code_summary_k")
    @SequenceGenerator(name = "discount_bucket_code_summary_k_seq", sequenceName = "discount_bucket_code_summary_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_bucket_code_summary_k_seq")
    @Exclude
    @ToString.Exclude
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk", nullable = false)
    private DiscountEntity discount;

    @NotNull
    @Column(name = "available_codes")
    private Long availableCodes;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    public DiscountBucketCodeSummaryEntity() {
    }

    public DiscountBucketCodeSummaryEntity(DiscountEntity discount,
                                           Long availableCodes) {
        this.discount = discount;
        this.availableCodes = availableCodes;
    }

}
