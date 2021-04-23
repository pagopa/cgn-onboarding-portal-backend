package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AgreementServiceLight {

    protected final AgreementRepository agreementRepository;

    public AgreementEntity findById(String agreementId) {
        return agreementRepository.findById(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Agreement not found"));
    }

    public void setFirstDiscountPublishingDate(AgreementEntity agreementEntity) {
        agreementEntity.setFirstDiscountPublishingDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    @Autowired
    public AgreementServiceLight(AgreementRepository agreementRepository) {
        this.agreementRepository = agreementRepository;
    }
}
