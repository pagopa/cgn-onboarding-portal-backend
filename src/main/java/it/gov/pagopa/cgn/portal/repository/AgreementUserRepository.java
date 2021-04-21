package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementUserRepository extends JpaRepository<AgreementUserEntity, String> {

}