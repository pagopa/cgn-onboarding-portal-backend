package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "discount_bucket_code_summary")
@Data
public class DiscountBucketCodeSummaryEntity
        extends BaseEntity
        implements Serializable {

    @Id
    @Column(name = "discount_fk")
    private Long id;

    @NotNull
    @Column(name = "total_codes")
    private Long totalCodes;

    @NotNull
    @Column(name = "available_codes")
    private Long availableCodes;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk")
    private DiscountEntity discount;

    public DiscountBucketCodeSummaryEntity() {
    }

    public DiscountBucketCodeSummaryEntity(DiscountEntity discount) {
        this.id = discount.getId();
        this.totalCodes = 0L;
        this.availableCodes = 0L;
    }

}
