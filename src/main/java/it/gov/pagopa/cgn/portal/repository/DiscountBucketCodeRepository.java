package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.custom.DiscountBucketCodeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscountBucketCodeRepository
        extends JpaRepository<DiscountBucketCodeEntity, Long>, DiscountBucketCodeRepositoryCustom {

    long countByDiscountAndBucketCodeLoadId(DiscountEntity discount, Long bucketCodeLoadId);

    List<DiscountBucketCodeEntity> findAllByDiscount(DiscountEntity discount);

    @Query(value = "SELECT COUNT(*) FROM discount_bucket_code WHERE discount_fk=:discount_id AND used=false", nativeQuery = true)
    long countNotUsedByDiscountId(@Param("discount_id") Long discountId);

    @Query(value = "SELECT * FROM discount_bucket_code WHERE discount_fk=:discount_id AND used=false LIMIT 1", nativeQuery = true)
    DiscountBucketCodeEntity getOneForDiscount(@Param("discount_id") Long discountId);

    @Modifying
    @Query(value = "delete from discount_bucket_code where discount_fk=:discount_id", nativeQuery = true)
    void deleteByDiscountId(@Param("discount_id") Long discountId);

    @Modifying
    @Query(value = "update discount_bucket_code set used = true where bucket_code_k=:bucket_code_k", nativeQuery = true)
    void burnDiscountBucketCode(@Param("bucket_code_k") Long bucketCodeId);
}