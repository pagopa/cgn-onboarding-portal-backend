package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "referents")
@Data
public class AAReferentEntity {

    @Id
    @NotNull
    @NotBlank
    @Size(max = 16)
    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @ToString.Exclude
    @OneToMany(mappedBy = "referent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AAOrganizationReferentEntity> organizationReferents;
}
