package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "document")
@Data
@TypeDef(name = "document_type_enum", typeClass = PostgreSQLEnumType.class)  // postgress enum type
public class DocumentEntity extends BaseEntity {

    @Id
    @NotNull
    @Column(name = "document_url")
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    @Type( type = "document_type_enum" )
    @NotNull
    private DocumentTypeEnum documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_fk", updatable = false, nullable = false)
    private AgreementEntity agreement;

    public OffsetDateTime getInsertTime() {
        return insertTime;
    }
}
