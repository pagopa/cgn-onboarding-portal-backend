package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "organizations")
@Data
public class AAOrganizationEntity implements Serializable {

    @Id
    @NotNull
    @NotBlank
    @Size(max = 16)
    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "pec", length = 100)
    private String pec;

    @NotNull
    @Column(name = "inserted_at")
    private OffsetDateTime insertedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AAOrganizationReferentEntity> organizationReferents;
}
