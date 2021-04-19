package it.gov.pagopa.repository;

import it.gov.pagopa.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {

    List<DiscountEntity> findByAgreementId(String agreementId);

}