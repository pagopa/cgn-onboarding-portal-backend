package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AAOrganizationReferentRepository extends JpaRepository<AAOrganizationReferentEntity, AAOrganizationReferentEntity> {
}
