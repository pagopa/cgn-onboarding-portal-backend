package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.model.PublishedProductCategoryViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface PublishedProductCategoryRepository extends JpaRepository<PublishedProductCategoryViewEntity, ProductCategoryEnum> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY published_product_category", nativeQuery = true)
    void refreshView();
}