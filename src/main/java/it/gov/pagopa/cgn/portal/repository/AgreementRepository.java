package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
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
           WHERE (
               a.state = :approvedState
               AND a.firstDiscountPublishingDate IS NULL
               AND a.startDate <= :cutoff
           )
           OR (
               a.state = :activeState
               AND EXISTS (
                   SELECT d.id
                   FROM DiscountEntity d
                   WHERE d.agreement = a
                     AND d.state = :publishedDiscountState
               )
               AND NOT EXISTS (
                   SELECT d.id
                   FROM DiscountEntity d
                   WHERE d.agreement = a
                     AND d.state = :publishedDiscountState
                     AND d.endDate > :cutoff
               )
           )
           """)
    List<AgreementEntity> findAgreementsToInactivate(@Param("cutoff") LocalDate cutoff,
                                                     @Param("approvedState") AgreementStateEnum approvedState,
                                                     @Param("activeState") AgreementStateEnum activeState,
                                                     @Param("publishedDiscountState") DiscountStateEnum publishedDiscountState);

}
