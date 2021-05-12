package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;


@Entity
@Table(name = "agreement")
@Data
@TypeDef(name = "agreement_state_enum", typeClass = PostgreSQLEnumType.class)  // postgress enum type
public class AgreementEntity extends BaseEntity {

    @Id
    @NotNull
    @NotBlank
    @Column(name = "agreement_k", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50)
    @Type( type = "agreement_state_enum" )
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

}

