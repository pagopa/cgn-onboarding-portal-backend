package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscountBucketCodeSummaryRepository
        extends JpaRepository<DiscountBucketCodeSummaryEntity, Long> {

    DiscountBucketCodeSummaryEntity findByDiscount(DiscountEntity discount);

    @Modifying
    @Query(value = "delete from discount_bucket_code_summary where discount_fk=:discount_id", nativeQuery = true)
    void deleteByDiscountId(@Param("discount_id") Long discountId);

    @Query(value = "select * from discount_bucket_code_summary bs join discount d on bs.discount_fk = d.discount_k where bs.expired_at is null and d.end_date >= CURRENT_DATE and d.state = 'PUBLISHED'",
           nativeQuery = true)
    List<DiscountBucketCodeSummaryEntity> findAllPublishedNotExpired();

    @Query(value = "select * from discount_bucket_code_summary bs join discount d on bs.discount_fk = d.discount_k where bs.expired_at <= CURRENT_TIMESTAMP and d.state = 'PUBLISHED'",
           nativeQuery = true)
    List<DiscountBucketCodeSummaryEntity> findAllPublishedAndExpired();
}