package it.gov.pagopa.repository;

import it.gov.pagopa.model.AgreementUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementUserRepository extends JpaRepository<AgreementUserEntity, String> {

}