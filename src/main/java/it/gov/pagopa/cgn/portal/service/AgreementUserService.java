package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgreementUserService {

    private final AgreementUserRepository agreementUserRepository;

    public Optional<AgreementUserEntity> findCurrentAgreementUser(String merchantTaxCode) {
        return agreementUserRepository.findById(merchantTaxCode);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementUserEntity create(String merchantTaxCode) {
        AgreementUserEntity userEntity = new AgreementUserEntity();
        userEntity.setUserId(merchantTaxCode);
        userEntity.setAgreementId(UUID.randomUUID().toString());
        return agreementUserRepository.save(userEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void updateMerchantTaxCode(String agreementId, String newMerchantTaxCode) {
        agreementUserRepository.updateMerchantTaxCode(agreementId, newMerchantTaxCode);
    }

    @Autowired
    public AgreementUserService(AgreementUserRepository userRepository) {
        this.agreementUserRepository = userRepository;
    }
}