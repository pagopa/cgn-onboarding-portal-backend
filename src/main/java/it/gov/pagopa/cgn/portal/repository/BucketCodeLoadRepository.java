package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BucketCodeLoadRepository extends JpaRepository<BucketCodeLoadEntity, Long> {
    
}
