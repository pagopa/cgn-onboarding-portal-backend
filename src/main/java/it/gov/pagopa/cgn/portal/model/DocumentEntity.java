package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;

import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @NotNull
    private DocumentTypeEnum documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_fk", updatable = false, nullable = false)
    private AgreementEntity agreement;

    public OffsetDateTime getInsertedDateTime() {
        return insertTime;
    }
}
