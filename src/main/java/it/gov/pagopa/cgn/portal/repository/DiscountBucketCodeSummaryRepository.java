package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface DiscountBucketCodeSummaryRepository
        extends JpaRepository<DiscountBucketCodeSummaryEntity, Long> {

    DiscountBucketCodeSummaryEntity findByDiscount(DiscountEntity discount);

    @Modifying
    @Query(value = "delete from discount_bucket_code_summary where discount_fk=:discount_id", nativeQuery = true)
    void deleteByDiscountId(@Param("discount_id") Long discountId);

    @Query("select bs from DiscountBucketCodeSummaryEntity bs join bs.discount d where bs.expiredAt is null and d.state = 'PUBLISHED'")
    List<DiscountBucketCodeSummaryEntity> findAllPublishedNotExpired();

    @Query("select bs from DiscountBucketCodeSummaryEntity bs join bs.discount d where bs.expiredAt <=  ?1 and d.state = 'PUBLISHED'")
    List<DiscountBucketCodeSummaryEntity> findAllPublishedAndExpired(
            OffsetDateTime thresholdDatetime);
}