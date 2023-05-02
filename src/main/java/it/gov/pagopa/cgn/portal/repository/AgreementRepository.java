package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AgreementRepository extends JpaRepository<AgreementEntity, String>, JpaSpecificationExecutor<AgreementEntity> {

}