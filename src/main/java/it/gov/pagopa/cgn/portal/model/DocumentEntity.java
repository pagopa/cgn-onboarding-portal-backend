package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "document")
@Data
public class DocumentEntity extends BaseEntity {

    @NotNull
    @NotBlank
    @Column(name = "agreement_fk", length = 36)
    private String agreementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    @NotNull
    private DocumentTypeEnum documentType;

    @Id
    @NotNull
    @Column(name = "document_url")
    private String documentUrl;
}
