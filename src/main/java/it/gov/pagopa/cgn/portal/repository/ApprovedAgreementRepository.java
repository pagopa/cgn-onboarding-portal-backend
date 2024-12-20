package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApprovedAgreementRepository
        extends JpaRepository<ApprovedAgreementEntity, String>, JpaSpecificationExecutor<ApprovedAgreementEntity> {

}