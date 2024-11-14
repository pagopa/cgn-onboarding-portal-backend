package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Immutable
@Table(name = "approved_agreements")
public class ApprovedAgreementEntity {

    @Id
    @NotNull
    @NotBlank
    @Column(name = "agreement_k")
    private String id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "information_last_update_date")
    private LocalDate informationLastUpdateDate;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ENUM)
    private AgreementStateEnum state;

    @Column(name = "assignee")
    private String backofficeAssignee;

    @Column(name = "request_approval_time")
    private OffsetDateTime requestApprovalTime;

    @Column(name = "published_discounts")
    private Long publishedDiscounts;

    @Column(name = "test_pending")
    private Boolean testPending;

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ENUM)
    private EntityTypeEnum entityType;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "agreement", fetch = FetchType.LAZY)
    private ProfileEntity profile;
}

