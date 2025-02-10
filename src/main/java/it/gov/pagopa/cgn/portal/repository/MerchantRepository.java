package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.MerchantViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface MerchantRepository
        extends JpaRepository<MerchantViewEntity, String> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY merchant", nativeQuery = true)
    void refreshView();
}