package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Document;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DocumentType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class BackofficeDocumentConverter extends AbstractConverter<DocumentEntity, Document> {

    private static final Map<DocumentTypeEnum, DocumentType> enumMap = new EnumMap<>(DocumentTypeEnum.class);
    static {
        enumMap.put(DocumentTypeEnum.AGREEMENT, DocumentType.AGREEMENT);
        enumMap.put(DocumentTypeEnum.MANIFESTATION_OF_INTEREST, DocumentType.MANIFESTATIONOFINTEREST);
    }

    @Override
    protected Function<DocumentEntity, Document> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Document, DocumentEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<DocumentEntity, Document> toDto =
            entity -> {
                Document dto = new Document();
                dto.setCreationDate(entity.getInsertDate());
                dto.setDocumentType(enumMap.get(entity.getDocumentType()));
                dto.setDocumentUrl(entity.getDocumentUrl());
                return dto;
            };
}
