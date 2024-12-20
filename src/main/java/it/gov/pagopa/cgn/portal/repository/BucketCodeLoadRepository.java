package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BucketCodeLoadRepository
        extends JpaRepository<BucketCodeLoadEntity, Long> {
    @Modifying
    @Query(value = "delete from bucket_code_load where discount_id=:discount_id", nativeQuery = true)
    void deleteByDiscountId(@Param("discount_id") Long discountId);

    BucketCodeLoadEntity findByUid(String uid);
}
