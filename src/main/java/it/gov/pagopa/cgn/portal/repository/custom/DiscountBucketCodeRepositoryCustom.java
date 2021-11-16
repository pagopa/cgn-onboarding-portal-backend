package it.gov.pagopa.cgn.portal.repository.custom;

import java.util.List;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;

public interface DiscountBucketCodeRepositoryCustom {

    public void bulkPersist(List<DiscountBucketCodeEntity> entities);

}
