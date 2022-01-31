package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface DiscountBucketCodeSummaryRepository
        extends JpaRepository<DiscountBucketCodeSummaryEntity, Long> {

    DiscountBucketCodeSummaryEntity findByDiscount(DiscountEntity discount);

    @Query("select bs from DiscountBucketCodeSummaryEntity bs join bs.discount d where bs.expiredAt is null and bs.availableCodes > 0 and d.state = 'PUBLISHED'")
    List<DiscountBucketCodeSummaryEntity> findAllPublishedByExpiredAtIsNullAndAvailableCodesGreaterThanZero();

    @Query("select bs from DiscountBucketCodeSummaryEntity bs join bs.discount d where bs.expiredAt <=  ?1 and bs.availableCodes > 0 and d.state = 'PUBLISHED'")
    List<DiscountBucketCodeSummaryEntity> findAllPublishedByExpiredAtLessThanEqualAndAvailableCodesGreaterZero(OffsetDateTime thresholdDatetime);
}