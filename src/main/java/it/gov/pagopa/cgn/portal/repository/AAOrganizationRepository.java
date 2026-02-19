package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AAOrganizationRepository extends JpaRepository<AAOrganizationEntity, String> {

    @Modifying
    @Query(value = "UPDATE organizations SET fiscal_code = :newFiscalCode WHERE fiscal_code = :oldFiscalCode",
           nativeQuery = true)
    int updateFiscalCode(@Param("oldFiscalCode") String oldFiscalCode, 
                         @Param("newFiscalCode") String newFiscalCode);

    @Query("SELECT DISTINCT o FROM AAOrganizationEntity o " +
           "INNER JOIN o.organizationReferents orgRef " +
           "WHERE (LOWER(o.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "       LOWER(o.fiscalCode) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<AAOrganizationEntity> findByNameOrFiscalCodeContainingIgnoreCase(
            @Param("searchQuery") String searchQuery,
            Pageable pageable);

    @Query("SELECT DISTINCT o FROM AAOrganizationEntity o " +
           "INNER JOIN o.organizationReferents orgRef " +
           "WHERE orgRef.referent.fiscalCode = :referentFiscalCode")
    Page<AAOrganizationEntity> findByReferentFiscalCode(
            @Param("referentFiscalCode") String referentFiscalCode,
            Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o) FROM AAOrganizationEntity o " +
           "INNER JOIN o.organizationReferents orgRef " +
           "WHERE (LOWER(o.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "       LOWER(o.fiscalCode) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    long countByNameOrFiscalCodeContainingIgnoreCase(@Param("searchQuery") String searchQuery);

    @Query("SELECT COUNT(DISTINCT o) FROM AAOrganizationEntity o " +
           "INNER JOIN o.organizationReferents orgRef " +
           "WHERE orgRef.referent.fiscalCode = :referentFiscalCode")
    long countByReferentFiscalCode(@Param("referentFiscalCode") String referentFiscalCode);

    @Query("SELECT DISTINCT o FROM AAOrganizationEntity o " +
           "INNER JOIN o.organizationReferents orgRef")
    Page<AAOrganizationEntity> findAllWithReferents(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o) FROM AAOrganizationEntity o " +
           "INNER JOIN o.organizationReferents orgRef")
    long countAllWithReferents();
}
