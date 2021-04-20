package it.gov.pagopa.model;

import it.gov.pagopa.enums.AgreementStateEnum;
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

    @Column(name = "profile_modified_date")
    private LocalDate profileModifiedDate;

    @Column(name = "discounts_modified_date")
    private LocalDate discountsModifiedDate;

    @Column(name = "documents_modified_date")
    private LocalDate documentsModifiedDate;

}

