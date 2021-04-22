package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Entity
@Table(name = "agreement")
@Data
public class AgreementEntity extends BaseEntity {

    @Id
    @NotNull
    @NotBlank
    @Column(name = "agreement_k", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50)
    @NotNull
    private AgreementStateEnum state;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "first_discount_publishing_date")
    private LocalDate firstDiscountPublishingDate;

    @Column(name = "reject_reason_msg", length = 500)
    private String rejectReasonMessage;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

}

