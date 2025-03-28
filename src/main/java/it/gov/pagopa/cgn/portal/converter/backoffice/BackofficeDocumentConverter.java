package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Document;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DocumentType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class BackofficeDocumentConverter
        extends AbstractConverter<DocumentEntity, Document> {

    private static final Map<DocumentTypeEnum, DocumentType> enumMap = new EnumMap<>(DocumentTypeEnum.class);
    private static final Map<String, DocumentTypeEnum> backofficeDocumentTypeMap = HashMap.newHashMap(2);

    static {
        enumMap.put(DocumentTypeEnum.BACKOFFICE_AGREEMENT, DocumentType.AGREEMENT);
        enumMap.put(DocumentTypeEnum.BACKOFFICE_ADHESION_REQUEST, DocumentType.ADHESION_REQUEST);
        enumMap.put(DocumentTypeEnum.AGREEMENT, DocumentType.AGREEMENT);
        enumMap.put(DocumentTypeEnum.ADHESION_REQUEST, DocumentType.ADHESION_REQUEST);
        backofficeDocumentTypeMap.put(DocumentType.AGREEMENT.getValue(), DocumentTypeEnum.BACKOFFICE_AGREEMENT);
        backofficeDocumentTypeMap.put(DocumentType.ADHESION_REQUEST.getValue(),
                                      DocumentTypeEnum.BACKOFFICE_ADHESION_REQUEST);
    }

    protected Function<String, DocumentTypeEnum> toBackofficeDocumentTypeEnum = documentTypeDto -> Optional.ofNullable(
            backofficeDocumentTypeMap.get(documentTypeDto)).orElseThrow(() -> getInvalidEnumMapping(documentTypeDto));
    protected Function<DocumentEntity, Document> toDto = entity -> {
        Document dto = new Document();
        dto.setCreationDate(entity.getInsertedDateTime().toLocalDate());
        dto.setDocumentType(Optional.ofNullable(enumMap.get(entity.getDocumentType()))
                                    .orElseThrow(() -> getInvalidEnumMapping(entity.getDocumentType().getCode())));
        dto.setDocumentUrl(entity.getDocumentUrl());
        return dto;
    };

    @Override
    protected Function<DocumentEntity, Document> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Document, DocumentEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public DocumentTypeEnum getBackofficeDocumentTypeEnum(String dtoDocumentType) {
        return toBackofficeDocumentTypeEnum.apply(dtoDocumentType);
    }
}
