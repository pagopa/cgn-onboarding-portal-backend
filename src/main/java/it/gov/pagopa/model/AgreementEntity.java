package it.gov.pagopa.model;

import it.gov.pagopa.enums.AgreementStateEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Entity
@Table(name = "AGREEMENT")
@Data
public class AgreementEntity extends BaseEntity {

    @Id
    @NotNull
    @NotBlank
    @Column(name = "AGREEMENT_K", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", length = 50)
    @NotNull
    private AgreementStateEnum state;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "PROFILE_MODIFIED_DATE")
    private LocalDate profileModifiedDate;

    @Column(name = "DISCOUNTS_MODIFIED_DATE")
    private LocalDate discountsModifiedDate;

    @Column(name = "DOCUMENTS_MODIFIED_DATE")
    private LocalDate documentsModifiedDate;

}

