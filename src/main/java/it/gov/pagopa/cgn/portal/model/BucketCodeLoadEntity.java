package it.gov.pagopa.cgn.portal.model;

import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import lombok.Data;

@Entity
@Table(name = "bucket_code_load", uniqueConstraints = @UniqueConstraint(columnNames = { "uid" }))
@Data
public class BucketCodeLoadEntity extends BaseEntity {

    @Id
    @Column(name = "bucket_code_load_k")
    @SequenceGenerator(name = "bucket_code_load_k_seq", sequenceName = "bucket_code_load_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bucket_code_load_k_seq")
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
    private String uid = UUID.randomUUID().toString();

    @NotNull
    @Column(name = "number_of_codes")
    private Long numberOfCodes;

    @NotNull
    @NotBlank
    @Column(name = "original_file_name")
    private String originalFileName;
}
