package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgreementService extends AgreementServiceLight {


    private final AgreementUserService userService;

    private final ProfileService profileService;

    private final DiscountService discountService;

    private final DocumentService documentService;

    private final AzureStorage azureStorage;

    private final EmailNotificationFacade emailNotificationFacade;

    private final BackofficeAgreementConverter backofficeAgreementConverter;

    private final ConfigProperties configProperties;
    
    @Transactional
    public AgreementEntity getAgreementByMerchantTaxCode(String merchantTaxCode){
        AgreementUserEntity userAgreement;
        Optional<AgreementUserEntity> userAgreementOpt = userService.findCurrentAgreementUser(merchantTaxCode);
        if (userAgreementOpt.isPresent()) {
            userAgreement = userAgreementOpt.get();
            return agreementRepository.findById(userAgreement.getAgreementId())
                    .orElseThrow(() -> new RuntimeException("User " + userAgreement.getUserId() + " doesn't have an agreement"));
        }  else {
            throw new InvalidRequestException("No Agreement User found with tax code " + merchantTaxCode);
        }

    }

    @Transactional
    public AgreementEntity createAgreementIfNotExists(String merchantTaxCode, EntityType entityType) {
        AgreementEntity agreementEntity;
        AgreementUserEntity userAgreement;
        Optional<AgreementUserEntity> userAgreementOpt = userService.findCurrentAgreementUser(merchantTaxCode);
        if (userAgreementOpt.isPresent()) {
            userAgreement = userAgreementOpt.get();
            // current user has already an agreement. Find it
            agreementEntity = agreementRepository.findById(userAgreement.getAgreementId())
                    .orElseThrow(() -> new RuntimeException("User " + userAgreement.getUserId() + " doesn't have an agreement"));
        } else {
            userAgreement = userService.create(merchantTaxCode);
            agreementEntity = createAgreement(userAgreement.getAgreementId(), entityType);
        }
        return agreementEntity;
    }

    @Transactional
    public AgreementEntity requestApproval(String agreementId) {
        AgreementEntity agreementEntity = findById(agreementId);

        ProfileEntity profile = profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Profile not found. Agreement not approvable"));
        List<DiscountEntity> discounts = discountService.getDiscounts(agreementId);
        if (CollectionUtils.isEmpty(discounts) && EntityTypeEnum.PRIVATE.equals(agreementEntity.getEntityType())) {
            throw new InvalidRequestException("Discounts not found. Agreement not approvable");
        }
        List<DocumentEntity> documents = documentService.getPrioritizedDocuments(agreementId);

        int nrDocs = agreementEntity.getEntityType().getNrDocs();

        if (documents == null || documents.size() != nrDocs) {
            throw new InvalidRequestException("Mandatory documents for "+agreementEntity.getEntityType()+":"+nrDocs
            											+", loaded: "+documents.size()+". Agreement not approvable");
        }
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setRequestApprovalTime(OffsetDateTime.now());

        var saved = agreementRepository.save(agreementEntity);

        emailNotificationFacade.notifyDepartmentNewAgreementRequest(profile.getFullName());

        return saved;
    }

    @Transactional
    public String uploadImage(String agreementId, MultipartFile image) {
        AgreementEntity agreementEntity = findById(agreementId);
        CGNUtils.validateImage(image, configProperties.getMinWidth(), configProperties.getMinHeight());
        String imageUrl = azureStorage.storeImage(agreementId, image);
        agreementEntity.setImageUrl(imageUrl);
        if (AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            setInformationLastUpdateDate(agreementEntity);
        }
        if (AgreementStateEnum.REJECTED.equals(agreementEntity.getState())) {
            setDraftAgreementFromRejected(agreementEntity);
            documentService.resetAllDocuments(agreementEntity.getId());
        }
        agreementRepository.save(agreementEntity);
        return imageUrl;
    }

    @Transactional(readOnly = true)
    public AgreementEntity getApprovedAgreement(String agreementId) {
        AgreementEntity agreementEntity = findById(agreementId);
        List<DiscountEntity> discounts = agreementEntity.getDiscountList();
        if (!CollectionUtils.isEmpty(discounts)) {
            discounts = discounts.stream()
                    .filter(d -> !DiscountStateEnum.DRAFT.equals(d.getState()) // not draft
                            && LocalDate.now().isBefore(d.getEndDate().plusDays(1))) // not expired
                    .collect(Collectors.toList());
            agreementEntity.setDiscountList(discounts);
        }
        agreementEntity.setDocumentList(documentService.getAllDocuments(agreementId,
                documentEntity -> documentEntity.getDocumentType().isBackoffice()));

        return agreementEntity;
    }

    private AgreementEntity createAgreement(String agreementId, EntityType entityType) {
        AgreementEntity agreementEntity = new AgreementEntity();
        agreementEntity.setId(agreementId);
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        EntityTypeEnum entityTypeEnum = backofficeAgreementConverter.toEntityEntityTypeEnum(entityType);
        agreementEntity.setEntityType(entityTypeEnum);
        return agreementRepository.save(agreementEntity);
    }

    @Autowired
    public AgreementService(AgreementRepository agreementRepository, AgreementUserService userService,
                            ProfileService profileService, DiscountService discountService,
                            DocumentService documentService, AzureStorage azureStorage,
                            EmailNotificationFacade emailNotificationFacade,
                            BackofficeAgreementConverter backofficeAgreementConverter, ConfigProperties configProperties) {
        super(agreementRepository);
        this.userService = userService;
        this.profileService = profileService;
        this.discountService = discountService;
        this.documentService = documentService;
        this.azureStorage = azureStorage;
        this.emailNotificationFacade = emailNotificationFacade;
        this.backofficeAgreementConverter = backofficeAgreementConverter;
        this.configProperties = configProperties;
    }


}

