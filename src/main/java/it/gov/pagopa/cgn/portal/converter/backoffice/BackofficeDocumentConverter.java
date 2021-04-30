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
public class BackofficeDocumentConverter extends AbstractConverter<DocumentEntity, Document> {

    private static final Map<DocumentTypeEnum, DocumentType> enumMap = new EnumMap<>(DocumentTypeEnum.class);
    private static final Map<String, DocumentTypeEnum> dtoDocumentTypeMap = new HashMap<>(2);

    static {
        enumMap.put(DocumentTypeEnum.BACKOFFICE_AGREEMENT, DocumentType.AGREEMENT);
        enumMap.put(DocumentTypeEnum.BACKOFFICE_MANIFESTATION_OF_INTEREST, DocumentType.MANIFESTATIONOFINTEREST);
        enumMap.put(DocumentTypeEnum.AGREEMENT, DocumentType.AGREEMENT);
        enumMap.put(DocumentTypeEnum.MANIFESTATION_OF_INTEREST, DocumentType.MANIFESTATIONOFINTEREST);
        dtoDocumentTypeMap.put("Agreement", DocumentTypeEnum.BACKOFFICE_AGREEMENT);
        dtoDocumentTypeMap.put("ManifestationOfInterest", DocumentTypeEnum.BACKOFFICE_MANIFESTATION_OF_INTEREST);
    }

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

    //todo review if is possible to reuse enumMap
    protected Function<String, DocumentTypeEnum> toBackofficeDocumentTypeEnum = documentTypeDto ->
            Optional.ofNullable(dtoDocumentTypeMap.get(documentTypeDto))
            .orElseThrow(() -> getInvalidEnumMapping(documentTypeDto));

    protected Function<DocumentEntity, Document> toDto =
            entity -> {
                Document dto = new Document();
                dto.setCreationDate(entity.getInsertDate());
                dto.setDocumentType(
                        Optional.ofNullable(enumMap.get(entity.getDocumentType()))
                                .orElseThrow(() -> getInvalidEnumMapping(entity.getDocumentType().getCode())));
                dto.setDocumentUrl(entity.getDocumentUrl());
                return dto;
            };
}
