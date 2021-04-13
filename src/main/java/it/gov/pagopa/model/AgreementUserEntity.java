package it.gov.pagopa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "AGREEMENT_USER")
@Data
public class AgreementUserEntity extends BaseEntity {

    @Id
    @NotBlank
    @NotNull
    @Column(name = "AGREEMENT_USER_K", length = 16)
    private String userId;

    @NotNull
    @NotBlank
    @Column(name = "AGREEMENT_ID", length = 36)
    private String subscriptionId;

    @PreUpdate
    @Override
    protected void onUpdate() {
        throw new UnsupportedOperationException("Cannot update an agreement user");
    }
}

