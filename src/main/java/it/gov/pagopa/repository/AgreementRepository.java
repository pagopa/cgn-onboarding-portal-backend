package it.gov.pagopa.repository;

import it.gov.pagopa.model.AgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<AgreementEntity, String> {

}