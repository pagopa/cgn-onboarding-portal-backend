package it.gov.pagopa.cgn.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCode;
import it.gov.pagopa.cgn.portal.repository.custom.DiscountBucketCodeRepositoryCustom;

public interface DiscountBucketCodeRepository
        extends JpaRepository<DiscountBucketCode, Long>, DiscountBucketCodeRepositoryCustom {

}