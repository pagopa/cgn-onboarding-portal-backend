package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgnonboardingportal.model.Document;
import it.gov.pagopa.cgnonboardingportal.model.Documents;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DocumentConverter extends AbstractConverter<DocumentEntity, Document> {


    @Override
    protected Function<DocumentEntity, Document> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Document, DocumentEntity> toEntityFunction() {
        throw new NotImplementedException("Conversion impossible");
    }

    protected Function<DocumentEntity, Document> toDto =
            entity -> {
                Document dto = new Document();
                dto.setDocumentType(entity.getDocumentType().getCode().toLowerCase());
                dto.setDocumentUrl(entity.getDocumentUrl());
                return dto;
            };


    public Documents getDocumentsDtoFromDocumentEntityList(List<DocumentEntity> documentEntityList) {
        return toDocumentsDto.apply(documentEntityList);
    }

    protected Function<List<DocumentEntity>, Documents> toDocumentsDto = documentEntities -> {
        List<Document> documentList = CollectionUtils.isEmpty(documentEntities) ?
                Collections.emptyList() : documentEntities.stream().map(toDtoFunction()).collect(Collectors.toList());
        Documents documents = new Documents();
        documents.setItems(documentList);
        return documents;
    };

}
