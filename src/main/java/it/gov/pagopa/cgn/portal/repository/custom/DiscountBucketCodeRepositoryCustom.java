package it.gov.pagopa.cgn.portal.repository.custom;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;

import java.util.List;

public interface DiscountBucketCodeRepositoryCustom {

    public void bulkPersist(List<DiscountBucketCodeEntity> entities);

}
