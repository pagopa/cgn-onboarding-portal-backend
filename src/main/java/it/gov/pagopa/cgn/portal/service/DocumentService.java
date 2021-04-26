package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.repository.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AzureStorage azureStorage;

    public List<DocumentEntity> getDocuments(String agreementId) {
        return documentRepository.findByAgreementId(agreementId);
    }

    public DocumentEntity storeDocument(String agreementId, DocumentTypeEnum documentType, InputStream content, long size) throws IOException {
        String url = azureStorage.storeDocument(agreementId, documentType, content, size);

        DocumentEntity document = new DocumentEntity();
        document.setDocumentUrl(url);
        document.setDocumentType(documentType);
        document.setAgreementId(agreementId);

        return documentRepository.save(document);
    }

    public void deleteDocument(String agreementId, DocumentTypeEnum documentType) {
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, documentType.toString());
    }

    public DocumentService(DocumentRepository documentRepository, AzureStorage azureStorage) {
        this.documentRepository = documentRepository;
        this.azureStorage = azureStorage;
    }

}
