package it.gov.pagopa.cgn.portal.support;

import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestReferentRepository
        extends JpaRepository<ReferentEntity, Long> {

    ReferentEntity findByProfileId(Long profileId);
}
