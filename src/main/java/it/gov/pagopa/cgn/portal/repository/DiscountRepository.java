package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {

    List<DiscountEntity> findByAgreementId(String agreementId);

    long countByAgreementIdAndState(String agreementId, DiscountStateEnum discountStateEnum);


    @Query(value = "SELECT * " +
            "FROM discount d " +
            "WHERE d.state = 'PUBLISHED' " +
            "AND d.expiration_15_days_warning IS NULL " +
            "AND d.end_date < now() + INTERVAL '15 days'",
            nativeQuery = true)
    List<DiscountEntity> findUnnotifiedExpiringDiscounts();
}