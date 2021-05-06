package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service
public class AgreementServiceLight {

    protected final AgreementRepository agreementRepository;

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity findById(String agreementId) {
        return agreementRepository.findById(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Agreement not found"));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void setFirstDiscountPublishingDate(AgreementEntity agreementEntity) {
        agreementEntity.setFirstDiscountPublishingDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void setInformationLastUpdateDate(AgreementEntity agreementEntity) {
        agreementEntity.setInformationLastUpdateDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    @Autowired
    public AgreementServiceLight(AgreementRepository agreementRepository) {
        this.agreementRepository = agreementRepository;
    }
}
