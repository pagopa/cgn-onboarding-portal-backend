package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.OnlineMerchantViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface OnlineMerchantRepository extends JpaRepository<OnlineMerchantViewEntity, String> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY online_merchant", nativeQuery = true)
    void refreshView();
}