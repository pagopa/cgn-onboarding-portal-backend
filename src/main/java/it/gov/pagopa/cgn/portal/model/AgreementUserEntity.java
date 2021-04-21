package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agreement_user")
@Data
public class AgreementUserEntity extends BaseEntity {

    @Id
    @NotBlank
    @NotNull
    @Column(name = "agreement_user_k", length = 16)
    private String userId;

    @NotNull
    @NotBlank
    @Column(name = "agreement_id", length = 36)
    private String agreementId;

    @PreUpdate
    @Override
    protected void onUpdate() {
        throw new UnsupportedOperationException("Cannot update an agreement user");
    }
}

