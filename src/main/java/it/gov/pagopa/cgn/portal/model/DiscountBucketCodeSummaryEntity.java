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
public class DiscountBucketCodeSummaryEntity implements Serializable {

    @Id
    @Column(name = "discount_fk")
    private Long id;

    @NotNull
    @Column(name = "available_codes")
    private Long availableCodes;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "discount_fk")
    private DiscountEntity discount;

    public DiscountBucketCodeSummaryEntity() {
    }

    public DiscountBucketCodeSummaryEntity(DiscountEntity discount) {
        this.id = discount.getId();
        this.availableCodes = 0L;
    }

}
