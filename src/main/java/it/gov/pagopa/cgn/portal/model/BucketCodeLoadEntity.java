package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "bucket_code_load", uniqueConstraints = @UniqueConstraint(columnNames = {"uid"}))
@Data
public class BucketCodeLoadEntity
        extends BaseEntity {

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
