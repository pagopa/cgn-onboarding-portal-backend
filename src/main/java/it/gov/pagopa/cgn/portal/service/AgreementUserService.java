package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgreementUserService {

    private final AgreementUserRepository userRepository;

    public Optional<AgreementUserEntity> findCurrentAgreementUser(String merchantTaxCode) {
        Optional<AgreementUserEntity> result = userRepository.findById(merchantTaxCode);
        return result;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementUserEntity create(String merchantTaxCode) {
        AgreementUserEntity userEntity = new AgreementUserEntity();
        userEntity.setUserId(merchantTaxCode);
        userEntity.setAgreementId(UUID.randomUUID().toString());
        return userRepository.save(userEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void updateMerchantTaxCode(String agreementId, String newMerchantTaxCode) {
        userRepository.updateMerchantTaxCode(agreementId, newMerchantTaxCode);
    }

    @Autowired
    public AgreementUserService(AgreementUserRepository userRepository) {
        this.userRepository = userRepository;
    }
}