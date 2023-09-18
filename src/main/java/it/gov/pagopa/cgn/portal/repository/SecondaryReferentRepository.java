package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecondaryReferentRepository extends JpaRepository<SecondaryReferentEntity,Long> {
    List<SecondaryReferentEntity> findByProfileId(Long profileId);

}
