package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AgreementService extends AgreementServiceLight {


    private final AgreementUserService userService;

    private final ProfileService profileService;

    private final DiscountService discountService;

    private final DocumentService documentService;
    private final AzureStorage azureStorage;

    private final ConfigProperties configProperties;

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity createAgreementIfNotExists(String merchantTaxCode) {
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
            agreementEntity = createAgreement(userAgreement.getAgreementId());
        }
        return agreementEntity;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public AgreementEntity requestApproval(String agreementId) {
        AgreementEntity agreementEntity = findById(agreementId);

        profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Profile not found. Agreement not approvable"));
        List<DiscountEntity> discounts = discountService.getDiscounts(agreementId);
        if (CollectionUtils.isEmpty(discounts)) {
            throw new InvalidRequestException("Discounts not found. Agreement not approvable");
        }
        List<DocumentEntity> documents = documentService.getPrioritizedDocuments(agreementId);
        if (documents == null || documents.size() < DocumentTypeEnum.getNumberOfDocumentProfile()) {
            throw new InvalidRequestException("Documents not or partially loaded. Agreement not approvable");
        }
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setRequestApprovalTime(OffsetDateTime.now());
        return agreementRepository.save(agreementEntity);
    }

    public String uploadImage(String agreementId, MultipartFile image) {
        AgreementEntity agreementEntity = findById(agreementId);
        CGNUtils.validateImage(image, configProperties.getMinWidth(), configProperties.getMinHeight());
        String imageUrl = azureStorage.storeImage(agreementId, image);
        agreementEntity.setImageUrl(imageUrl);
        if (AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            setInformationLastUpdateDate(agreementEntity);
        }
        agreementRepository.save(agreementEntity);
        return imageUrl;
    }

    private AgreementEntity createAgreement(String agreementId) {
        AgreementEntity agreementEntity = new AgreementEntity();
        agreementEntity.setId(agreementId);
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        return agreementRepository.save(agreementEntity);
    }

    @Autowired
    public AgreementService(AgreementRepository agreementRepository, AgreementUserService userService,
                            ProfileService profileService, DiscountService discountService,
                            DocumentService documentService, AzureStorage azureStorage,
                            ConfigProperties configProperties) {
        super(agreementRepository);
        this.userService = userService;
        this.profileService = profileService;
        this.discountService = discountService;
        this.documentService = documentService;
        this.azureStorage = azureStorage;
        this.configProperties = configProperties;
    }


}

