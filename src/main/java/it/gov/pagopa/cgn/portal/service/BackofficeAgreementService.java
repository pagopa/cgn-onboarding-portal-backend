package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackofficeAgreementService {

    public static final String FAKE_BACKOFFICE_ID = "FAKE";

    private final AgreementRepository agreementRepository;

    private final AgreementServiceLight agreementServiceLight;

    private final DocumentService documentService;

    @Transactional(readOnly = true)
    public Page<AgreementEntity> getAgreements(BackofficeFilter filter) {

        BackofficeAgreementToValidateSpecification spec;
        spec = new BackofficeAgreementToValidateSpecification(filter, FAKE_BACKOFFICE_ID);  //todo get from token
        Pageable pageable = spec.getPage();
        Page<AgreementEntity> agreementEntityPage = agreementRepository.findAll(spec, pageable);

        // exclude backoffice documents
        agreementEntityPage.getContent().forEach(agreementEntity -> {
            List<DocumentEntity> documents = agreementEntity.getDocumentList().stream()
                    .filter(d -> !d.getDocumentType().isBackoffice()).collect(Collectors.toList());
            agreementEntity.setDocumentList(documents);
        });
        return agreementEntityPage;
    }

    @Transactional
    public AgreementEntity assignAgreement(String agreementId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        validateForAssignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(FAKE_BACKOFFICE_ID);
        return agreementRepository.save(agreementEntity);
    }

    @Transactional
    public AgreementEntity unassignAgreement(String agreementId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        validateForUnassignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(null);
        return agreementRepository.save(agreementEntity);
    }

    @Transactional
    public AgreementEntity approveAgreement(String agreementId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        checkPendingStatus(agreementEntity);
        checkAgreementIsAssignedToCurrentUser(agreementEntity);
        List<DocumentEntity> documents = documentService.getAllDocuments(agreementId);
        if (CollectionUtils.isEmpty(documents) || documents.size() != DocumentTypeEnum.values().length) {
            throw new InvalidRequestException("Not all documents are loaded");
        }
        agreementEntity.setRejectReasonMessage(null);
        agreementEntity.setStartDate(LocalDate.now());
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        //TODO SEND NOTIFICATION
        return agreementRepository.save(agreementEntity);
    }


    @Transactional
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
