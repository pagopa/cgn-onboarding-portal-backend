package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.BackofficeAgreementToValidateSpecification;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementTerminationAction;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class BackofficeAgreementService {

    private final AgreementRepository agreementRepository;
    private final AgreementServiceLight agreementServiceLight;
    private final DocumentService documentService;
    private final EmailNotificationFacade emailNotificationFacade;
    private final AzureStorage azureStorage;
    private final Collection<DocumentTypeEnum> mandatoryDocuments = Stream.of(DocumentTypeEnum.AGREEMENT,
                                                                              DocumentTypeEnum.ADHESION_REQUEST,
                                                                              DocumentTypeEnum.BACKOFFICE_AGREEMENT)
                                                                          .toList();
    private final Collection<DocumentTypeEnum> mandatoryPaDocuments = Stream.of(DocumentTypeEnum.AGREEMENT,
                                                                                DocumentTypeEnum.BACKOFFICE_AGREEMENT)
                                                                            .toList();

    @Autowired
    public BackofficeAgreementService(AgreementRepository agreementRepository,
                                      AgreementServiceLight agreementServiceLight,
                                      DocumentService documentService,
                                      EmailNotificationFacade emailNotificationFacade,
                                      AzureStorage azureStorage) {
        this.agreementRepository = agreementRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.documentService = documentService;
        this.emailNotificationFacade = emailNotificationFacade;
        this.azureStorage = azureStorage;
    }

    @Transactional(readOnly = true)
    public Page<AgreementEntity> getAgreements(BackofficeFilter filter) {

        BackofficeAgreementToValidateSpecification spec;
        spec = new BackofficeAgreementToValidateSpecification(filter, CGNUtils.getJwtAdminUserName());
        Page<AgreementEntity> agreementEntityPage = agreementRepository.findAll(spec, spec.getPage());

        // exclude backoffice documents
        agreementEntityPage.getContent().forEach(agreementEntity -> {
            List<DocumentEntity> documents = agreementEntity.getDocumentList()
                                                            .stream()
                                                            .filter(d -> !d.getDocumentType().isBackoffice())
                                                            .toList();
            //setting SAS Url
            azureStorage.setSecureDocumentUrl(documents);
            agreementEntity.setDocumentList(documents);
        });
        return agreementEntityPage;
    }

    @Transactional
    public AgreementEntity assignAgreement(String agreementId) {
        var agreementEntity = agreementServiceLight.findAgreementById(agreementId);
        validateForAssignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        return agreementRepository.save(agreementEntity);
    }

    @Transactional
    public AgreementEntity unassignAgreement(String agreementId) {
        var agreementEntity = agreementServiceLight.findAgreementById(agreementId);
        validateForUnassignment(agreementEntity);
        agreementEntity.setBackofficeAssignee(null);
        return agreementRepository.save(agreementEntity);
    }

    @Transactional
    public AgreementEntity approveAgreement(String agreementId) {

        AgreementEntity agreementEntity = agreementServiceLight.findAgreementById(agreementId);
        checkPendingStatus(agreementEntity);
        checkAgreementIsAssignedToCurrentUser(agreementEntity);
        List<DocumentEntity> documents = documentService.getAllDocuments(agreementId);

        Collection<DocumentTypeEnum> manDocs = EntityTypeEnum.PRIVATE.equals(agreementEntity.getEntityType()) ?
                                               mandatoryDocuments:
                                               mandatoryPaDocuments;

        if (CollectionUtils.isEmpty(documents) ||
            !documents.stream().map(DocumentEntity::getDocumentType).toList().containsAll(manDocs)) {
            throw new InvalidRequestException(ErrorCodeEnum.MANDATORY_DOCUMENT_ARE_MISSING.getValue());
        }

        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        agreementEntity.setRejectReasonMessage(null);
        agreementEntity.setStartDate(currentDate);
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setInformationLastUpdateDate(currentDate);  //default equals to start date
        agreementEntity = agreementRepository.save(agreementEntity);

        var profile = agreementEntity.getProfile();
        emailNotificationFacade.notifyMerchantAgreementRequestApproved(profile,
                                                                       profile.getSalesChannel(),
                                                                       Optional.ofNullable(profile.getDiscountCodeType()));

        return agreementEntity;
    }

    @Transactional
    public AgreementEntity rejectAgreement(String agreementId, String reasonMessage) {
        var agreementEntity = agreementServiceLight.findAgreementById(agreementId);
        checkPendingStatus(agreementEntity);

        agreementEntity.setRejectReasonMessage(reasonMessage);
        agreementEntity.setState(AgreementStateEnum.REJECTED);

        agreementEntity = agreementRepository.save(agreementEntity);

        ProfileEntity profileEntity = agreementEntity.getProfile();
        emailNotificationFacade.notifyMerchantAgreementRequestRejected(profileEntity, reasonMessage);

        return agreementEntity;
    }

    @Transactional
    public AgreementEntity manageAgreementTermination(String agreementId, AgreementTerminationAction action) {
        AgreementEntity agreementEntity = agreementServiceLight.findAgreementById(agreementId);
        switch (action) {
            case SEND_TERMINATION_REMINDER:
                return updateAgreementState(agreementEntity,
                                            AgreementStateEnum.TERMINATION_REMINDER_SENT,
                                            EnumSet.of(AgreementStateEnum.INACTIVE),
                                            action);
            case START_TERMINATION_IN_PROGRESS:
                return updateAgreementState(agreementEntity,
                                            AgreementStateEnum.TERMINATION_IN_PROGRESS,
                                            EnumSet.of(AgreementStateEnum.TERMINATION_REMINDER_SENT),
                                            action);
            case CANCEL_TERMINATION_IN_PROGRESS:
                return updateAgreementState(agreementEntity,
                                            AgreementStateEnum.TERMINATION_REMINDER_SENT,
                                            EnumSet.of(AgreementStateEnum.TERMINATION_IN_PROGRESS),
                                            action);
            case COMPLETE_TERMINATION:
                return updateAgreementState(agreementEntity,
                                            AgreementStateEnum.TERMINATED,
                                            EnumSet.of(AgreementStateEnum.TERMINATION_IN_PROGRESS),
                                            action);
            default:
                throw new InvalidRequestException("Unsupported termination action: " + action);
        }
    }

    private AgreementEntity updateAgreementState(AgreementEntity agreementEntity,
                                                 AgreementStateEnum targetState,
                                                 EnumSet<AgreementStateEnum> allowedStates,
                                                 AgreementTerminationAction action) {
        checkAgreementState(agreementEntity, allowedStates, action);
        return updateAgreementState(agreementEntity, targetState);
    }

    private AgreementEntity updateAgreementState(AgreementEntity agreementEntity,
                                                 AgreementStateEnum targetState) {
        agreementEntity.setState(targetState);
        agreementEntity.setInformationLastUpdateDate(LocalDate.now(ZoneOffset.UTC));
        return agreementRepository.save(agreementEntity);
    }

    private void checkAgreementState(AgreementEntity agreementEntity,
                                     EnumSet<AgreementStateEnum> allowedStates,
                                     AgreementTerminationAction action) {
        if (!allowedStates.contains(agreementEntity.getState())) {
            throw new InvalidRequestException(String.format("Cannot execute %s for agreement in state %s",
                                                            action.getValue(),
                                                            agreementEntity.getState().getCode()));
        }
    }

    private void validateForUnassignment(AgreementEntity agreementEntity) {
        checkPendingStatus(agreementEntity);
        if (StringUtils.isBlank(agreementEntity.getBackofficeAssignee())) {
            throw new InvalidRequestException(ErrorCodeEnum.AGREEMENT_NO_LONGER_ASSIGNED.getValue());
        }
        checkAgreementIsAssignedToCurrentUser(agreementEntity);
    }

    private void checkAgreementIsAssignedToCurrentUser(AgreementEntity agreementEntity) {
        if (!CGNUtils.getJwtAdminUserName().equals(agreementEntity.getBackofficeAssignee())) {
            throw new InvalidRequestException(ErrorCodeEnum.AGREEMENT_NOT_ASSIGNED_TO_CURRENT_USER.getValue());
        }
    }

    private void validateForAssignment(AgreementEntity agreementEntity) {
        checkPendingStatus(agreementEntity);
        if (!StringUtils.isBlank(agreementEntity.getBackofficeAssignee())) {
            if (CGNUtils.getJwtAdminUserName().equals(agreementEntity.getBackofficeAssignee())) {
                throw new InvalidRequestException(ErrorCodeEnum.AGREEMENT_ALREADY_ASSIGNED_TO_CURRENT_USER.getValue());
            }
            log.info(String.format("User %s is being assigned the agreement %s currently assigned to user %s",
                                   CGNUtils.getJwtAdminUserName(),
                                   agreementEntity.getId(),
                                   agreementEntity.getBackofficeAssignee()));
        }
    }

    private void checkPendingStatus(AgreementEntity agreementEntity) {
        if (!AgreementStateEnum.PENDING.equals(agreementEntity.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_AGREEMENT_NOT_IN_PENDING.getValue());
        }
    }
}
