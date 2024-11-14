package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AgreementServiceLight {

    protected final AgreementRepository agreementRepository;

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity findAgreementById(String agreementId) {
        return  getAgreementById(agreementId)
                                  .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.AGREEMENT_NOT_FOUND.getValue()));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Optional<AgreementEntity> getAgreementById(String agreementId) {
        return agreementRepository.findById(agreementId);
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

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity setDraftAgreementFromRejected(AgreementEntity agreement) {
        agreement.setState(AgreementStateEnum.DRAFT);
        agreement.setRequestApprovalTime(null);
        agreement.setStartDate(null);
        agreement.setEndDate(null);
        agreement.setRejectReasonMessage(null);
        agreement.setBackofficeAssignee(null);
        return agreementRepository.save(agreement);
    }

    @Autowired
    public AgreementServiceLight(AgreementRepository agreementRepository) {
        this.agreementRepository = agreementRepository;
    }
}
