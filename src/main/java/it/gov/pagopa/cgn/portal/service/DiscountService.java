package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.ConflictErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;
import it.gov.pagopa.cgn.portal.util.ValidationUtils;
import it.gov.pagopa.cgn.portal.wrapper.CrudDiscountWrapper;
import it.gov.pagopa.cgnonboardingportal.model.DiscountBucketCodeLoadingProgess;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private static final int MAX_NUMBER_PUBLISHED_DISCOUNT = 5;

    private final DiscountRepository discountRepository;
    private final AgreementServiceLight agreementServiceLight;
    private final ProfileService profileService;
    private final EmailNotificationFacade emailNotificationFacade;
    private final DocumentService documentService;
    private final ValidatorFactory factory;
    private final BucketService bucketService;
    private final DiscountBucketCodeRepository discountBucketCodeRepository;
    private final DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository;
    private final ConfigProperties configProperties;
    private final BucketLoadUtils bucketLoadUtils;
    private final OfflineMerchantRepository offlineMerchantRepository;
    private final OnlineMerchantRepository onlineMerchantRepository;
    private final PublishedProductCategoryRepository publishedProductCategoryRepository;

    @Transactional(Transactional.TxType.REQUIRED)
    public CrudDiscountWrapper createDiscount(String agreementId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        AgreementEntity agreement = agreementServiceLight.findById(agreementId);
        discountEntity.setAgreement(agreement);
        ProfileEntity profileEntity = validateDiscount(agreementId, discountEntity, true);
        DiscountEntity toReturn = discountRepository.save(discountEntity);
        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())) {
            bucketService.prepareDiscountBucketCodeSummary(toReturn);
            bucketService.createPendingBucketLoad(toReturn);
        }
        return new CrudDiscountWrapper(toReturn, profileEntity.getDiscountCodeType());
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<DiscountEntity> getDiscounts(String agreementId) {
        return discountRepository.findByAgreementId(agreementId);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity getDiscountById(String agreementId, Long discountId) {
        Optional<DiscountEntity> discountEntityOptional = discountRepository.findById(discountId);
        if (discountEntityOptional.isEmpty() ||
            !agreementId.equals(discountEntityOptional.get().getAgreement().getId())) {
            throw new InvalidRequestException("Discount not found or agreement is invalid");
        }
        return discountEntityOptional.get();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public CrudDiscountWrapper updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        DiscountEntity dbEntity = findById(discountId);
        var agreementEntity = dbEntity.getAgreement();
        if (!agreementId.equals(agreementEntity.getId())) {
            throw new InvalidRequestException("Agreement not found");
        }

        checkDiscountRelatedSameAgreement(dbEntity, agreementId);
        DiscountCodeTypeEnum profileDiscountType = profileService.getProfile(agreementId)
                                                                 .orElseThrow(() -> new InvalidRequestException(
                                                                         "Cannot create discount without a profile"))
                                                                 .getDiscountCodeType();

        boolean isChangedBucketLoad = DiscountCodeTypeEnum.BUCKET.equals(profileDiscountType) &&
                                      ((dbEntity.getLastBucketCodeLoad() == null &&
                                        discountEntity.getLastBucketCodeLoadUid() != null) ||
                                       !dbEntity.getLastBucketCodeLoad()
                                                .getUid()
                                                .equals(discountEntity.getLastBucketCodeLoadUid()));

        if (isChangedBucketLoad &&
            dbEntity.getLastBucketCodeLoad() != null &&
            bucketService.isLastBucketLoadStillLoading(dbEntity.getLastBucketCodeLoad().getId())) {
            throw new ConflictErrorException("Cannot update discount bucket while another bucket processing is running");
        }

        updateConsumer.accept(discountEntity, dbEntity);
        validateDiscount(agreementId, dbEntity, isChangedBucketLoad);

        if (isChangedBucketLoad) {
            bucketService.prepareDiscountBucketCodeSummary(dbEntity);
            dbEntity = bucketService.createPendingBucketLoad(dbEntity);
        }

        // if state is Published, last modify must be updated because public information
        // was modified
        if (DiscountStateEnum.PUBLISHED.equals(dbEntity.getState())) {
            agreementServiceLight.setInformationLastUpdateDate(agreementEntity);
            dbEntity.setExpirationWarningSentDateTime(null);
        }

        // updating suspended discount: move to draft status
        if (DiscountStateEnum.SUSPENDED.equals(dbEntity.getState())) {
            dbEntity.setState(DiscountStateEnum.DRAFT);
        }

        if (AgreementStateEnum.DRAFT.equals(agreementEntity.getState())) {
            documentService.resetMerchantDocuments(agreementId);
        }

        if (AgreementStateEnum.REJECTED.equals(agreementEntity.getState())) {
            agreementEntity = agreementServiceLight.setDraftAgreementFromRejected(agreementEntity);
            documentService.resetAllDocuments(agreementId);
        }

        discountEntity.setAgreement(agreementEntity);
        discountRepository.save(dbEntity);
        return new CrudDiscountWrapper(dbEntity, profileDiscountType, isChangedBucketLoad);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteDiscount(String agreementId, Long discountId) {
        // check if agreement exits. If not the method throw an exception
        agreementServiceLight.findById(agreementId);
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();

        discountRepository.deleteById(discountId);

        // refresh materialized views
        refreshMaterializedViews(profileEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity testDiscount(String agreementId, Long discountId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        DiscountEntity discount = findById(discountId);

        // check sales channel
        if (SalesChannelEnum.OFFLINE.equals(discount.getAgreement().getProfile().getSalesChannel())) {
            throw new ConflictErrorException("Cannot test discounts for offline merchants.");
        }

        validateTestingDiscount(agreementEntity, discount);

        discount.setState(DiscountStateEnum.TEST_PENDING);
        discount = discountRepository.save(discount);

        emailNotificationFacade.notifyDepartementToTestDiscount(discount.getAgreement().getProfile().getFullName(),
                                                                discount.getName(),
                                                                discount.getAgreement()
                                                                        .getProfile()
                                                                        .getDiscountCodeType()
                                                                        .getCode());

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity publishDiscount(String agreementId, Long discountId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        DiscountEntity discount = findById(discountId);
        // we should update start date to "now" if it's in the past
        if (LocalDate.now().isAfter(discount.getStartDate())) {
            discount.setStartDate(LocalDate.now());
        }
        validatePublishingDiscount(agreementEntity, discount);
        discount.setState(DiscountStateEnum.PUBLISHED);
        discount = discountRepository.save(discount);
        agreementServiceLight.setInformationLastUpdateDate(agreementEntity);
        // check if exists almost one discount already published
        if (agreementEntity.getFirstDiscountPublishingDate() == null) {
            long numPublishedDiscount = discountRepository.countByAgreementIdAndState(agreementId,
                                                                                      DiscountStateEnum.PUBLISHED);
            if (numPublishedDiscount == 1) { // 1 -> discount just created
                agreementServiceLight.setFirstDiscountPublishingDate(agreementEntity);
            }
        }

        // refresh materialized views
        refreshMaterializedViews(profileEntity);

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity unpublishDiscount(String agreementId, Long discountId) {
        DiscountEntity discount = findById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.PUBLISHED.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot unpublish a discount not public");
        }
        discount.setState(DiscountStateEnum.DRAFT);
        discount = discountRepository.save(discount);

        // refresh materialized views
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        refreshMaterializedViews(profileEntity);

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity suspendDiscount(String agreementId, Long discountId, String reasonMessage) {
        DiscountEntity discount = findById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.PUBLISHED.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot suspend a discount not Public");
        }
        discount.setState(DiscountStateEnum.SUSPENDED);
        discount.setSuspendedReasonMessage(reasonMessage);
        discount = discountRepository.save(discount);
        // send notification
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        emailNotificationFacade.notifyMerchantDiscountSuspended(profileEntity.getReferent().getEmailAddress(),
                                                                discount.getName(),
                                                                reasonMessage);

        // refresh materialized views
        refreshMaterializedViews(profileEntity);

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public String getDiscountBucketCode(String agreementId, Long discountId) {
        DiscountEntity discount = findById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.TEST_PENDING.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot get a code for a discount not in test");
        }

        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        if (!DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())) {
            throw new InvalidRequestException("Cannot get a code for a discount with no bucket codes");
        }

        DiscountBucketCodeEntity bucketCodeEntity = discountBucketCodeRepository.getOneForDiscount(discountId);
        discountBucketCodeRepository.burnDiscountBucketCode(bucketCodeEntity.getId());

        return bucketCodeEntity.getCode();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void setDiscountTestPassed(String agreementId, Long discountId) {
        DiscountEntity discount = findById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.TEST_PENDING.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot apply to a discount not in test");
        }
        discount.setTestFailureReason(null);
        discount.setState(DiscountStateEnum.TEST_PASSED);
        discount = discountRepository.save(discount);

        // send notification
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        emailNotificationFacade.notifyMerchantDiscountTestPassed(profileEntity.getReferent().getEmailAddress(),
                                                                 discount.getName());
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void setDiscountTestFailed(String agreementId, Long discountId, String reasonMessage) {
        DiscountEntity discount = findById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.TEST_PENDING.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot apply to a discount not in test");
        }
        discount.setState(DiscountStateEnum.TEST_FAILED);
        discount.setTestFailureReason(reasonMessage);
        discount = discountRepository.save(discount);

        // send notification
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        emailNotificationFacade.notifyMerchantDiscountTestFailed(profileEntity.getReferent().getEmailAddress(),
                                                                 discount.getName(),
                                                                 reasonMessage);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void refreshMaterializedViews(ProfileEntity profileEntity) {
        publishedProductCategoryRepository.refreshView();
        switch (profileEntity.getSalesChannel()) {
            case ONLINE:
                onlineMerchantRepository.refreshView();
                break;
            case OFFLINE:
                offlineMerchantRepository.refreshView();
                break;
            case BOTH:
                onlineMerchantRepository.refreshView();
                offlineMerchantRepository.refreshView();
                break;
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void sendNotificationDiscountExpiring(DiscountEntity discount) {
        String referentEmailAddress = discount.getAgreement().getProfile().getReferent().getEmailAddress();

        emailNotificationFacade.notifyMerchantDiscountExpiring(referentEmailAddress, discount.getName());
        discount.setExpirationWarningSentDateTime(OffsetDateTime.now());
        discountRepository.save(discount);
    }

    @Autowired
    public DiscountService(DiscountRepository discountRepository,
                           AgreementServiceLight agreementServiceLight,
                           ProfileService profileService,
                           EmailNotificationFacade emailNotificationFacade,
                           DocumentService documentService,
                           ValidatorFactory factory,
                           BucketService bucketService,
                           DiscountBucketCodeRepository discountBucketCodeRepository,
                           DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository,
                           ConfigProperties configProperties,
                           BucketLoadUtils bucketLoadUtils,
                           OfflineMerchantRepository offlineMerchantRepository,
                           OnlineMerchantRepository onlineMerchantRepository,
                           PublishedProductCategoryRepository publishedProductCategoryRepository) {
        this.discountRepository = discountRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.profileService = profileService;
        this.emailNotificationFacade = emailNotificationFacade;
        this.documentService = documentService;
        this.factory = factory;
        this.bucketService = bucketService;
        this.discountBucketCodeRepository = discountBucketCodeRepository;
        this.discountBucketCodeSummaryRepository = discountBucketCodeSummaryRepository;
        this.configProperties = configProperties;
        this.bucketLoadUtils = bucketLoadUtils;
        this.offlineMerchantRepository = offlineMerchantRepository;
        this.onlineMerchantRepository = onlineMerchantRepository;
        this.publishedProductCategoryRepository = publishedProductCategoryRepository;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountBucketCodeLoadingProgess getDiscountBucketCodeLoadingProgess(String agreementId, Long discountId) {
        DiscountEntity discountEntity = getDiscountById(agreementId, discountId);
        var loadedCodes = bucketService.countLoadedCodes(discountEntity);
        var percent = Float.valueOf(loadedCodes) /
                      Float.valueOf(discountEntity.getLastBucketCodeLoad().getNumberOfCodes()) * 100;
        var progress = new DiscountBucketCodeLoadingProgess();
        progress.setLoaded(loadedCodes);
        progress.setPercent(percent);
        return progress;
    }

    private DiscountEntity findById(Long discountId) {
        return discountRepository.findById(discountId)
                                 .orElseThrow(() -> new InvalidRequestException("Discount not found"));
    }

    private void checkDiscountRelatedSameAgreement(DiscountEntity discountEntity, String agreementId) {
        if (!agreementId.equals(discountEntity.getAgreement().getId())) {
            throw new InvalidRequestException("Discount is not related to agreement provided");
        }
    }

    private ProfileEntity validateDiscount(String agreementId,
                                           DiscountEntity discountEntity,
                                           boolean isBucketFileChanged) {
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException(
                                                            "Cannot create discount without a profile"));

        commonDiscountValidation(profileEntity, discountEntity, isBucketFileChanged);

        ValidationUtils.performConstraintValidation(factory.getValidator(), discountEntity);
        return profileEntity;
    }

    private void validatePublishingDiscount(AgreementEntity agreementEntity, DiscountEntity discount) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId())
                                                    .orElseThrow(() -> new InvalidRequestException(
                                                            "Cannot get discount's profile"));

        // perform common discount validation to keep entities coherent
        commonDiscountValidation(profileEntity, discount, false);

        // perform testing specific validation
        validateTestingDiscount(profileEntity, agreementEntity, discount);

        // perform publishing specific validation
        validatePublishingDiscount(profileEntity, discount);
    }

    private void validatePublishingDiscount(ProfileEntity profileEntity, DiscountEntity discount) {
        //perform publishing specific validation
        if (!SalesChannelEnum.OFFLINE.equals(profileEntity.getSalesChannel()) &&
            !DiscountStateEnum.TEST_PASSED.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot proceed with an online discount that's not passed a test");
        }
    }

    private void validateTestingDiscount(AgreementEntity agreementEntity, DiscountEntity discount) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId())
                                                    .orElseThrow(() -> new InvalidRequestException(
                                                            "Cannot get discount's profile"));

        // perform common discount validation to keep entities coherent
        commonDiscountValidation(profileEntity, discount, false);

        // perform testing specific validation
        validateTestingDiscount(profileEntity, agreementEntity, discount);
    }

    private void validateTestingDiscount(ProfileEntity profileEntity,
                                         AgreementEntity agreementEntity,
                                         DiscountEntity discount) {
        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType()) &&
            (discount.getLastBucketCodeLoad() == null ||
             bucketService.isLastBucketLoadStillLoading(discount.getLastBucketCodeLoad().getId()))) {
            throw new ConflictErrorException("Cannot proceed with a discount with a bucket load in progress");
        }
        if (!AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            throw new InvalidRequestException("Cannot proceed with a discount with a not approved agreement");
        }
        if (DiscountStateEnum.SUSPENDED.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot proceed with a suspended discount");
        }
        if (!isContainsToday(agreementEntity.getStartDate(), agreementEntity.getEndDate())) {
            throw new InvalidRequestException("Cannot proceed with a discount because the agreement is expired");
        }

        if (LocalDate.now().isAfter(discount.getEndDate())) {
            throw new InvalidRequestException("Cannot proceed with an expired discount");
        }
        checkDiscountRelatedSameAgreement(discount, agreementEntity.getId());
        long publishedDiscount = discountRepository.countByAgreementIdAndState(agreementEntity.getId(),
                                                                               DiscountStateEnum.PUBLISHED);
        if (publishedDiscount >= MAX_NUMBER_PUBLISHED_DISCOUNT) {
            throw new InvalidRequestException("Cannot proceed with the discount because there are already " +
                                              MAX_NUMBER_PUBLISHED_DISCOUNT +
                                              " public ones");
        }
    }

    private void commonDiscountValidation(ProfileEntity profileEntity,
                                          DiscountEntity discountEntity,
                                          boolean isBucketFileChanged) {
        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType()) &&
            StringUtils.isBlank(discountEntity.getStaticCode())) {
            throw new InvalidRequestException(
                    "Discount cannot have empty static code for a profile with discount code type static");
        }

        if (DiscountCodeTypeEnum.LANDINGPAGE.equals(profileEntity.getDiscountCodeType()) &&
            (StringUtils.isBlank(discountEntity.getLandingPageUrl()))) {
            throw new InvalidRequestException(
                    "Discount cannot have empty landing page url for a profile with discount code type landingpage");
        }

        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType()) &&
            isBucketFileChanged &&
            (discountEntity.getLastBucketCodeLoadUid() == null ||
             !bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()))) {
            throw new InvalidRequestException(
                    "Discount cannot reference to empty or not existing bucket file for a profile with discount code type bucket");
        }

        // If profile use API, static code and landing page will not used
        if (DiscountCodeTypeEnum.API.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
            discountEntity.setLastBucketCodeLoadFileName(null);
            discountEntity.setLastBucketCodeLoad(null);
            bucketLoadUtils.deleteBucketCodes(discountEntity.getId());
        }

        // If profile use STATIC, landing page will not used
        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
            discountEntity.setLastBucketCodeLoadFileName(null);
            discountEntity.setLastBucketCodeLoad(null);
            bucketLoadUtils.deleteBucketCodes(discountEntity.getId());
        }

        // If profile use LANDINGPAGE, static code will not used
        if (DiscountCodeTypeEnum.LANDINGPAGE.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLastBucketCodeLoadUid(null);
            discountEntity.setLastBucketCodeLoadFileName(null);
            discountEntity.setLastBucketCodeLoad(null);
            bucketLoadUtils.deleteBucketCodes(discountEntity.getId());
        }

        // If profile use BUCKET, others will not used
        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
        }

        // If profile sales channel is OFFLINE, any discount is visible on eyca
        // and all online stuff should be cleaned
        if (SalesChannelEnum.OFFLINE.equals(profileEntity.getSalesChannel())) {
            discountEntity.setVisibleOnEyca(true);
            discountEntity.setStaticCode(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
            discountEntity.setLastBucketCodeLoadFileName(null);
            discountEntity.setLastBucketCodeLoad(null);
            bucketLoadUtils.deleteBucketCodes(discountEntity.getId());
        }
    }

    private final BiConsumer<DiscountEntity, List<DiscountProductEntity>> updateProducts
            = (discountEntity, productsToUpdate) -> {
        // add all products from DTO. If there are products already present will be
        // skipped
        discountEntity.addProductList(productsToUpdate);

        // search and remove (if there are) products deleted by user
        List<DiscountProductEntity> toDeleteProduct = discountEntity.getProducts()
                                                                    .stream()
                                                                    .filter(prodEntity -> !productsToUpdate.contains(
                                                                            prodEntity))
                                                                    .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(toDeleteProduct)) {
            toDeleteProduct.forEach(discountEntity::removeProduct);
        }
    };

    private final BiConsumer<DiscountEntity, DiscountEntity> updateConsumer = (toUpdateEntity, dbEntity) -> {
        dbEntity.setName(toUpdateEntity.getName());
        dbEntity.setDescription(toUpdateEntity.getDescription());
        dbEntity.setStartDate(toUpdateEntity.getStartDate());
        dbEntity.setEndDate(toUpdateEntity.getEndDate());
        dbEntity.setDiscountValue(toUpdateEntity.getDiscountValue());
        updateProducts.accept(dbEntity, toUpdateEntity.getProducts());
        dbEntity.setCondition(toUpdateEntity.getCondition());
        dbEntity.setStaticCode(toUpdateEntity.getStaticCode());
        dbEntity.setVisibleOnEyca(toUpdateEntity.getVisibleOnEyca());
        dbEntity.setLandingPageUrl(toUpdateEntity.getLandingPageUrl());
        dbEntity.setLandingPageReferrer(toUpdateEntity.getLandingPageReferrer());
        dbEntity.setLastBucketCodeLoadUid(toUpdateEntity.getLastBucketCodeLoadUid());
        dbEntity.setLastBucketCodeLoadFileName(toUpdateEntity.getLastBucketCodeLoadFileName());
        dbEntity.setDiscountUrl(toUpdateEntity.getDiscountUrl());
    };

    private boolean isContainsToday(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        return (!now.isBefore(startDate)) && (now.isBefore(endDate));
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void suspendDiscountIfDiscountBucketCodesAreExpired(DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity) {
        var discountBucketCodeSummary
                = discountBucketCodeSummaryRepository.getOne(discountBucketCodeSummaryEntity.getId());
        DiscountEntity discount = discountBucketCodeSummary.getDiscount();
        suspendDiscount(discount.getAgreement().getId(),
                        discount.getId(),
                        "La lista di codici è esaurita da più di " +
                        configProperties.getSuspendDiscountsWithoutAvailableBucketCodesAfterDays() +
                        " giorni");
    }
}
