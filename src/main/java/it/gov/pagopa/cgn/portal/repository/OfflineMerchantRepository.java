package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.OfflineMerchantViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface OfflineMerchantRepository
        extends JpaRepository<OfflineMerchantViewEntity, String> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY offline_merchant", nativeQuery = true)
    void refreshView();
}