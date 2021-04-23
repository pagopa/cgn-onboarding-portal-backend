package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {

    List<DiscountEntity> findByAgreementId(String agreementId);

    @Query("select count(*) from DiscountEntity d where d.agreement.id =:agreementId and d.state =it.gov.pagopa.cgn.portal.enums.DiscountStateEnum.PUBLISHED")
    long countPublishedDiscountByAgreementId(String agreementId);

}