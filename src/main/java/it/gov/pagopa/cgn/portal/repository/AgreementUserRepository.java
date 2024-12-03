package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgreementUserRepository
        extends JpaRepository<AgreementUserEntity, String> {

    @Modifying
    @Query(value = "update agreement_user set agreement_user_k=:new_merchant_tax_code where agreement_id=:agreement_id",
           nativeQuery = true)
    int updateMerchantTaxCode(@Param("agreement_id") String agreementId,
                              @Param("new_merchant_tax_code") String newMerchantTaxCode);
}