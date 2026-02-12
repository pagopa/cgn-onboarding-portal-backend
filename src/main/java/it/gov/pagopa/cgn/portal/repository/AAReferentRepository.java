package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AAReferentRepository extends JpaRepository<AAReferentEntity, String> {
}
