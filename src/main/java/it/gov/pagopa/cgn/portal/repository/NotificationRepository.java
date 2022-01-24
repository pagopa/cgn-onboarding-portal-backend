package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<NotificationEntity, Long> {
    NotificationEntity findByKey(String key);
}