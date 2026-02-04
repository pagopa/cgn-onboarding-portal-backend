package it.gov.pagopa.cgn.portal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "organizations_referents")
@IdClass(AAOrganizationReferentId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AAOrganizationReferentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_fiscal_code", referencedColumnName = "fiscal_code")
    @NotNull
    private AAOrganizationEntity organization;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referent_fiscal_code", referencedColumnName = "fiscal_code")
    @NotNull
    private AAReferentEntity referent;

    @NotNull
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
