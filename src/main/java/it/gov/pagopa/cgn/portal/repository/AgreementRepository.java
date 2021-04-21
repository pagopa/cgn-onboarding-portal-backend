package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<AgreementEntity, String> {

}