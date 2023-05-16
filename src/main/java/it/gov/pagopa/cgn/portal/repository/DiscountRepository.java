package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {

    List<DiscountEntity> findByAgreementId(String agreementId);

    long countByAgreementIdAndState(String agreementId, DiscountStateEnum discountStateEnum);

    long countByAgreementIdAndStateAndEndDateGreaterThan(String agreementId, DiscountStateEnum discountStateEnum, LocalDate aDate);

    List<DiscountEntity> findByStateAndExpirationWarningSentDateTimeIsNullAndEndDateLessThan(
            DiscountStateEnum discountStateEnum, LocalDate endDate);
}