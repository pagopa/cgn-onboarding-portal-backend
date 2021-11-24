package it.gov.pagopa.cgn.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;

public interface BucketCodeLoadRepository extends JpaRepository<BucketCodeLoadEntity, Long> {
    BucketCodeLoadEntity findByDiscountIdAndUid(Long discountId, String uid);
}
