package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.OnlineMerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface OnlineMerchantRepository extends JpaRepository<OnlineMerchantEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY online_merchant", nativeQuery = true)
    void refreshView();
}