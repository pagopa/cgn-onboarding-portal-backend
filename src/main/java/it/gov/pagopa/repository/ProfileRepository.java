package it.gov.pagopa.repository;

import it.gov.pagopa.model.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    boolean existsProfileEntityByAgreementId(String agreementId);

}