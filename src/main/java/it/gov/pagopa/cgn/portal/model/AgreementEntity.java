package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;


@Entity
@Table(name = "agreement")
@Data
@Convert(attributeName = "state", converter = PostgreSQLEnumType.class)  // postgress enum type
@Convert(attributeName = "entityType", converter = PostgreSQLEnumType.class)  // postgress enum type
public class  AgreementEntity extends BaseEntity {

    @Id
    @NotNull
    @NotBlank
    @Column(name = "agreement_k", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50)
    @JdbcTypeCode(SqlTypes.ENUM)
    @NotNull
    private AgreementStateEnum state;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "first_discount_publishing_date")
    private LocalDate firstDiscountPublishingDate;

    @Size(max = 500)
    @Column(name = "reject_reason_msg", length = 500)
    private String rejectReasonMessage;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "agreement", fetch = FetchType.LAZY)
    private ProfileEntity profile;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "agreement", fetch = FetchType.LAZY)
    private List<DiscountEntity> discountList;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "agreement", fetch = FetchType.LAZY)
    private List<DocumentEntity> documentList;

    @Size(max = 100)
    @Column(name = "assignee", length = 100)
    private String backofficeAssignee;

    @Column(name = "request_approval_time")
    private OffsetDateTime requestApprovalTime;

    @Column(name = "information_last_update_date")
    private LocalDate informationLastUpdateDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 24)
    @JdbcTypeCode(SqlTypes.ENUM)
    private EntityTypeEnum entityType;


}

