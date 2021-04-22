package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByAgreementId(String agreementId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM document d " +
            "WHERE d.agreement_k = :agreement_id "+
            "AND cast(d.document_type as text) = :document_type", nativeQuery = true)
    void deleteByAgreementIdAndDocumentType(@Param("agreement_id") String agreementId, @Param("document_type") String documentType);

}
