package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeAuditRepository
        extends JpaRepository<ChangeAuditEntity, Long> {
}