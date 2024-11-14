package it.gov.pagopa.cgn.portal.model;

import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode.Exclude;

@Entity
@Table(name = "bucket_code_load", uniqueConstraints = @UniqueConstraint(columnNames = { "uid" }))
@Data
public class BucketCodeLoadEntity extends BaseEntity {

    @Id
    @Column(name = "bucket_code_load_k")
    @SequenceGenerator(name = "bucket_code_load_k_seq", sequenceName = "bucket_code_load_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bucket_code_load_k_seq")
    @Exclude
    @ToString.Exclude
    private Long id;

    @NotNull
    @Column(name = "discount_id")
    private Long discountId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private BucketCodeLoadStatusEnum status;

    @NotNull
    @NotBlank
    @Column(name = "uid")
    @Size(max = 255)
    private String uid = UUID.randomUUID().toString();

    @Exclude
    @ToString.Exclude
    @Column(name = "number_of_codes")
    private Long numberOfCodes;

    @NotNull
    @NotBlank
    @Column(name = "file_name")
    @Size(max = 255)
    private String fileName;
}
