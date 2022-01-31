package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountBucketCodeSummaryRepository
        extends JpaRepository<DiscountBucketCodeSummaryEntity, Long> {

    DiscountBucketCodeSummaryEntity findByDiscount(DiscountEntity discount);

    List<DiscountBucketCodeSummaryEntity> findAllByExpiredAtIsNull();

    List<DiscountBucketCodeSummaryEntity> findAllByExpiredAtIsNotNull();
}