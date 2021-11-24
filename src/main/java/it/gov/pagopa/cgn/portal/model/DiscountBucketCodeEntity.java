package it.gov.pagopa.cgn.portal.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.EqualsAndHashCode.Exclude;

@Entity
@Table(name = "discount_bucket_code")
@Data
public class DiscountBucketCodeEntity implements Serializable {

    @Id
    @Column(name = "bucket_code_k")
    @SequenceGenerator(name = "discount_bucket_code_k_seq", sequenceName = "discount_bucket_code_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_bucket_code_k_seq")
    @Exclude
    @ToString.Exclude
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 20)
    @Column(name = "code", length = 20)
    private String code;

    @NotNull
    @Column(name = "used")
    private Boolean isUsed = false;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk", nullable = false)
    private DiscountEntity discount;

    @NotNull
    @Column(name = "bucket_code_load_id")
    private Long bucketCodeLoadId;

    public DiscountBucketCodeEntity() {
    }

    public DiscountBucketCodeEntity(@NotNull @NotBlank @Size(max = 20) String code, DiscountEntity discount,
            Long bucketCodeLoadId) {
        this.code = code;
        this.discount = discount;
        this.bucketCodeLoadId = bucketCodeLoadId;
    }

}
