package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.BackofficeAgreementToValidateSpecification;
import it.gov.pagopa.cgn.portal.repository.BackofficeApprovedAgreementSpecification;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BackofficeAgreementService {

    private final AgreementRepository agreementRepository;

    private final AgreementServiceLight agreementServiceLight;

    private final DocumentService documentService;

    private final EmailNotificationFacade emailNotificationFacade;

    private final AzureStorage azureStorage;

    @Transactional(readOnly = true)
    public Page<AgreementEntity> getAgreements(BackofficeFilter filter) {

        BackofficeAgreementToValidateSpecification spec;
        spec = new BackofficeAgreementToValidateSpecification(filter, CGNUtils.getJwtAdminUserName());
        Page<AgreementEntity> agreementEntityPage = agreementRepository.findAll(spec, spec.getPage());

        // exclude backoffice documents
        agreementEntityPage.getContent().forEach(agreementEntity -> {
            List<DocumentEntity> documents = agreementEntity.getDocumentList().stream()
                    .filter(d -> !d.getDocumentType().isBackoffice()).collect(Collectors.toList());
            //setting SAS Url
            azureStorage.setSecureDocumentUrl(documents);
            agreementEntity.setDocumentList(documents);
        });
        return agreementEntityPage;
    }

    @Transactional(readOnly = true)
    public Page<AgreementEntity> getApprovedAgreements(BackofficeFilter filter) {

        BackofficeApprovedAgreementSpecification spec;
        spec = new BackofficeApprovedAgreementSpecification(filter, CGNUtils.getJwtAdminUserName());
        return agreementRepository.findAll(spec, spec.getPage());
    }

    @Transactional
    public AgreementEntity assignAgreement(String agreementId) {
        var agreementEntity = agreementServiceLight.findById(agreementId);
        validateForAssignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        return agreementRepository.save(agreementEntity);
    }

    @Transactional
    public AgreementEntity unassignAgreement(String agreementId) {
        var agreementEntity = agreementServiceLight.findById(agreementId);
        validateForUnassignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(null);
        return agreementRepository.save(agreementEntity);
    }

    @Transactional
    public AgreementEntity approveAgreement(String agreementId) {
        var agreementEntity = agreementServiceLight.findById(agreementId);
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
        agreementEntity.setInformationLastUpdateDate(LocalDate.now());  //default equals to start date
        agreementEntity = agreementRepository.save(agreementEntity);

        var profile = agreementEntity.getProfile();
        String referentEmail = profile.getReferent().getEmailAddress();
        emailNotificationFacade.notifyMerchantAgreementRequestApproved(
                referentEmail,
                profile.getSalesChannel(),
                Optional.ofNullable(profile.getDiscountCodeType())
        );

        return agreementEntity;
    }


    @Transactional
    public AgreementEntity rejectAgreement(String agreementId, String reasonMessage) {
        var agreementEntity = agreementServiceLight.findById(agreementId);
        checkPendingStatus(agreementEntity);

        agreementEntity.setRejectReasonMessage(reasonMessage);
        agreementEntity.setState(AgreementStateEnum.REJECTED);

        agreementEntity = agreementRepository.save(agreementEntity);

        String referentEmail = agreementEntity.getProfile().getReferent().getEmailAddress();
        emailNotificationFacade.notifyMerchantAgreementRequestRejected(referentEmail, reasonMessage);

        return agreementEntity;
    }

    @Autowired
    public BackofficeAgreementService(AgreementRepository agreementRepository,
                                      AgreementServiceLight agreementServiceLight, DocumentService documentService,
                                      EmailNotificationFacade emailNotificationFacade, AzureStorage azureStorage) {
        this.agreementRepository = agreementRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.documentService = documentService;
        this.emailNotificationFacade = emailNotificationFacade;
        this.azureStorage = azureStorage;
    }


    private static final String AGREEMENT_LABEL = "Agreement ";

    private void validateForUnassignment(AgreementEntity agreementEntity) {
        checkPendingStatus(agreementEntity);
        if (StringUtils.isBlank(agreementEntity.getBackofficeAssignee())) {
            throw new InvalidRequestException(AGREEMENT_LABEL + agreementEntity.getId() + " isn't assigned to anymore");
        }
        checkAgreementIsAssignedToCurrentUser(agreementEntity);
    }

    private void checkAgreementIsAssignedToCurrentUser(AgreementEntity agreementEntity) {
        if (!CGNUtils.getJwtAdminUserName().equals(agreementEntity.getBackofficeAssignee())) {
            throw new InvalidRequestException(AGREEMENT_LABEL + agreementEntity.getId() + " isn't assigned to current user");
        }
    }

    private void validateForAssignment(AgreementEntity agreementEntity) {
        checkPendingStatus(agreementEntity);
        if (!StringUtils.isBlank(agreementEntity.getBackofficeAssignee())) {
            if (CGNUtils.getJwtAdminUserName().equals(agreementEntity.getBackofficeAssignee())) {
                throw new InvalidRequestException(AGREEMENT_LABEL + agreementEntity.getId() + " is already assigned to current user");
            }
            log.info(String.format(
                    "User %s is being assigned the agreement %s currently assigned to user %s",
                    CGNUtils.getJwtAdminUserName(), agreementEntity.getId(), agreementEntity.getBackofficeAssignee()));
        }
    }

    private void checkPendingStatus(AgreementEntity agreementEntity) {
        if (!AgreementStateEnum.PENDING.equals(agreementEntity.getState())) {
            throw new InvalidRequestException(AGREEMENT_LABEL + agreementEntity.getId() +
                    " haven't the state expected. Status found: " + agreementEntity.getState());
        }
    }
}
