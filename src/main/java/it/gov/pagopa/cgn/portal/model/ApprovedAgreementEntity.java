package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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
    @Type(type = "agreement_state_enum")
    private AgreementStateEnum state;

    @Column(name = "published_discounts")
    private Long publishedDiscounts;

}

