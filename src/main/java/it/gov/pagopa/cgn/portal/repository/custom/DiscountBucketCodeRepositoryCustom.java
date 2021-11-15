package it.gov.pagopa.cgn.portal.repository.custom;

import java.util.List;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCode;

public interface DiscountBucketCodeRepositoryCustom {

    public void bulkPersist(List<DiscountBucketCode> entities);

}
