package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository
        extends JpaRepository<ProfileEntity, Long> {

    boolean existsProfileEntityByAgreementId(String agreementId);

    Optional<ProfileEntity> findByAgreementId(String agreementId);

}