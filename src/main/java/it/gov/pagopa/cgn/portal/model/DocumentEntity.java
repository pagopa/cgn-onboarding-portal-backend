package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "document")
@Data
public class DocumentEntity extends BaseEntity {

    @Id
    @NotNull
    @Column(name = "document_url")
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    @NotNull
    private DocumentTypeEnum documentType;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_fk", updatable = false, nullable = false, unique = true)
    private AgreementEntity agreement;

    public LocalDate getInsertDate() {
        return insertTime.toLocalDate();
    }
}
