package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.BackofficeAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BackofficeAgreementRepository extends JpaRepository<BackofficeAgreementEntity, String>, JpaSpecificationExecutor {

}