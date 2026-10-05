package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgreementRepository
        extends JpaRepository<AgreementEntity, String>, JpaSpecificationExecutor {

    @Query("""
           SELECT DISTINCT a
           FROM AgreementEntity a
           LEFT JOIN FETCH a.profile
           WHERE a.state = it.gov.pagopa.cgn.portal.enums.AgreementStateEnum.ACTIVE
               AND NOT EXISTS (
                           SELECT d.id
                           FROM DiscountEntity d
                           WHERE d.agreement = a
                               AND d.state = it.gov.pagopa.cgn.portal.enums.DiscountStateEnum.PUBLISHED
                               AND d.endDate >= :currentDate
               )
           """)
    List<AgreementEntity> findActiveAgreementsToExpire(@Param("currentDate") LocalDate currentDate);

    @Query("""
           SELECT DISTINCT a
           FROM AgreementEntity a
           LEFT JOIN FETCH a.profile
           WHERE a.state = it.gov.pagopa.cgn.portal.enums.AgreementStateEnum.EXPIRED
               AND NOT EXISTS (
                           SELECT d.id
                           FROM DiscountEntity d
                           WHERE d.agreement = a
                               AND d.state = it.gov.pagopa.cgn.portal.enums.DiscountStateEnum.PUBLISHED
                               AND d.endDate >= :currentDate
               )
           """)
    List<AgreementEntity> findExpiredAgreementsWithoutValidDiscounts(@Param("currentDate") LocalDate currentDate);

}
