package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByAgreementId(String agreementId);

    long deleteByAgreementIdAndDocumentType(String agreementId, DocumentTypeEnum documentType);

}
