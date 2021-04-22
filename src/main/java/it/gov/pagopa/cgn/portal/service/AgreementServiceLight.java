package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgreementServiceLight {

    protected final AgreementRepository agreementRepository;

    public AgreementEntity findById(String agreementId) {
        return agreementRepository.findById(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Agreement not found"));
    }

    @Autowired
    public AgreementServiceLight(AgreementRepository agreementRepository) {
        this.agreementRepository = agreementRepository;
    }
}
