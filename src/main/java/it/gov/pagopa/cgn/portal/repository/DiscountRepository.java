package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository
        extends JpaRepository<DiscountEntity, Long> {

    List<DiscountEntity> findByAgreementId(String agreementId);

    Optional<DiscountEntity> findByEycaUpdateId(String eycaUpdateId);

    long countByAgreementIdAndState(String agreementId, DiscountStateEnum discountStateEnum);

    long countByAgreementIdAndStateAndEndDateGreaterThan(String agreementId,
                                                         DiscountStateEnum discountStateEnum,
                                                         LocalDate aDate);
    @Query(value = " SELECT d FROM DiscountEntity d WHERE d.state = :state  AND d.expirationWarningSentDateTime IS NULL AND d.endDate > CURRENT_DATE AND d.endDate < :maxDate")
    List<DiscountEntity> findDiscountsExpiringSoon(@Param("state") DiscountStateEnum state,@Param("maxDate") LocalDate maxDate);

}