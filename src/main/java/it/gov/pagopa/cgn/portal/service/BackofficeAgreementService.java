package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.BackofficeAgreementToValidateSpecification;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class BackofficeAgreementService {

    public static final String FAKE_BACKOFFICE_ID = "FAKE";

    private final AgreementRepository agreementRepository;

    private final AgreementServiceLight agreementServiceLight;

    private final DocumentService documentService;

    @Transactional(Transactional.TxType.REQUIRED)
    public Page<AgreementEntity> getAgreements(BackofficeFilter filter) {
        BackofficeAgreementToValidateSpecification spec;
        spec = new BackofficeAgreementToValidateSpecification(filter, FAKE_BACKOFFICE_ID);  //todo get from token
        Pageable page = spec.getPage();
        return agreementRepository.findAll(spec, page);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity assignAgreement(String agreementId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        validateForAssignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(FAKE_BACKOFFICE_ID);
        return agreementRepository.save(agreementEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity unassignAgreement(String agreementId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        validateForUnassignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(null);
        return agreementRepository.save(agreementEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity approveAgreement(String agreementId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        checkPendingStatus(agreementEntity);
        checkAgreementIsAssignedToCurrentUser(agreementEntity);
        List<DocumentEntity> documents = documentService.getDocuments(agreementId);
        //todo check admin document
        agreementEntity.setRejectReasonMessage(null);
        agreementEntity.setStartDate(LocalDate.now());
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        //TODO SEND NOTIFICATION
        return agreementRepository.save(agreementEntity);
    }


    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity rejectAgreement(String agreementId, String reasonMessage) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        checkPendingStatus(agreementEntity);
        //TODO SEND NOTIFICATION
        agreementEntity.setRejectReasonMessage(reasonMessage);
        agreementEntity.setState(AgreementStateEnum.REJECTED);
        return agreementRepository.save(agreementEntity);
    }

    private void validateForUnassignment(AgreementEntity agreementEntity) {
        checkPendingStatus(agreementEntity);
        if (StringUtils.isBlank(agreementEntity.getBackofficeAssignee())) {
            throw new InvalidRequestException("Agreement " + agreementEntity.getId() + " isn't assigned to anymore");
        }
        checkAgreementIsAssignedToCurrentUser(agreementEntity);
    }

    private void checkAgreementIsAssignedToCurrentUser(AgreementEntity agreementEntity) {
        if (!FAKE_BACKOFFICE_ID.equals(agreementEntity.getBackofficeAssignee())) {
            throw new InvalidRequestException("Agreement " + agreementEntity.getId() + " isn't assigned to current user");
        }
    }

    private void validateForAssignment(AgreementEntity agreementEntity) {
        checkPendingStatus(agreementEntity);
        if (!StringUtils.isBlank(agreementEntity.getBackofficeAssignee())) {
            if (FAKE_BACKOFFICE_ID.equals(agreementEntity.getBackofficeAssignee())) {
                throw new InvalidRequestException("Agreement " + agreementEntity.getId() + " is already assigned to current user");
            }
            //todo to understand if this check is required
            throw new InvalidRequestException("Agreement " + agreementEntity.getId() + " is assigned to another user");
        }
    }

    private void checkPendingStatus(AgreementEntity agreementEntity) {
        if (!AgreementStateEnum.PENDING.equals(agreementEntity.getState())) {
            throw new InvalidRequestException("Agreement " + agreementEntity.getId() +
                    " haven't the state expected. Status found: " + agreementEntity.getState());
        }
    }

    @Autowired
    public BackofficeAgreementService(AgreementRepository agreementRepository,
                                      AgreementServiceLight agreementServiceLight, DocumentService documentService) {
        this.agreementRepository = agreementRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.documentService = documentService;
    }
}
