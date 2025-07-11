package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;
import it.gov.pagopa.cgn.portal.util.ValidationUtils;
import it.gov.pagopa.cgn.portal.wrapper.CrudDiscountWrapper;
import it.gov.pagopa.cgnonboardingportal.model.DiscountBucketCodeLoadingProgess;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.apache.commons.lang3.StringUtils;
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
    private final BucketLoadUtils bucketLoadUtils;
    private final MerchantRepository merchantRepository;
    private final OfflineMerchantRepository offlineMerchantRepository;
    private final OnlineMerchantRepository onlineMerchantRepository;
    private final PublishedProductCategoryRepository publishedProductCategoryRepository;
    private final BiConsumer<DiscountEntity, List<DiscountProductEntity>> updateProducts = (discountEntity, productsToUpdate) -> {
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
        if ((dbEntity.getStaticCode()!=null && !dbEntity.getStaticCode().equals(toUpdateEntity.getStaticCode())) ||
            (dbEntity.getEycaLandingPageUrl()!=null &&
             !dbEntity.getEycaLandingPageUrl().equals(toUpdateEntity.getEycaLandingPageUrl()))) {
            dbEntity.setEycaEmailUpdateRequired(true);
        }
        dbEntity.setName(toUpdateEntity.getName());
        dbEntity.setNameEn(toUpdateEntity.getNameEn());
        dbEntity.setNameDe(toUpdateEntity.getNameDe());
        dbEntity.setDescription(toUpdateEntity.getDescription());
        dbEntity.setDescriptionEn(toUpdateEntity.getDescriptionEn());
        dbEntity.setDescriptionDe(toUpdateEntity.getDescriptionDe());
        dbEntity.setStartDate(toUpdateEntity.getStartDate());
        dbEntity.setEndDate(toUpdateEntity.getEndDate());
        dbEntity.setDiscountValue(toUpdateEntity.getDiscountValue());
        updateProducts.accept(dbEntity, toUpdateEntity.getProducts());
        dbEntity.setCondition(toUpdateEntity.getCondition());
        dbEntity.setConditionEn(toUpdateEntity.getConditionEn());
        dbEntity.setConditionDe(toUpdateEntity.getConditionDe());
        dbEntity.setStaticCode(toUpdateEntity.getStaticCode());
        dbEntity.setVisibleOnEyca(toUpdateEntity.getVisibleOnEyca());
        dbEntity.setLandingPageUrl(toUpdateEntity.getLandingPageUrl());
        dbEntity.setEycaLandingPageUrl(toUpdateEntity.getEycaLandingPageUrl());
        dbEntity.setLandingPageReferrer(toUpdateEntity.getLandingPageReferrer());
        dbEntity.setLastBucketCodeLoadUid(toUpdateEntity.getLastBucketCodeLoadUid());
        dbEntity.setLastBucketCodeLoadFileName(toUpdateEntity.getLastBucketCodeLoadFileName());
        dbEntity.setDiscountUrl(toUpdateEntity.getDiscountUrl());
    };

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
                           BucketLoadUtils bucketLoadUtils,
                           MerchantRepository merchantRepository,
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
        this.bucketLoadUtils = bucketLoadUtils;
        this.merchantRepository = merchantRepository;
        this.offlineMerchantRepository = offlineMerchantRepository;
        this.onlineMerchantRepository = onlineMerchantRepository;
        this.publishedProductCategoryRepository = publishedProductCategoryRepository;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public CrudDiscountWrapper createDiscount(String agreementId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        AgreementEntity agreement = agreementServiceLight.findAgreementById(agreementId);
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
        if (discountEntityOptional.isEmpty()) {
            throw new InvalidRequestException(ErrorCodeEnum.DISCOUNT_NOT_FOUND.getValue());
        }
        DiscountEntity entity = discountEntityOptional.get();
        checkDiscountRelatedSameAgreement(entity, agreementId);

        return entity;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public CrudDiscountWrapper updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        AgreementEntity agreementEntity = agreementServiceLight.findAgreementById(agreementId);

        DiscountEntity dbEntity = findDiscountById(discountId);
        checkDiscountRelatedSameAgreement(dbEntity, agreementId);

        ProfileEntity profile = profileService.getProfile(agreementId)
                                              .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        DiscountCodeTypeEnum profileDiscountType = profile.getDiscountCodeType();

        boolean isChangedBucketLoad = DiscountCodeTypeEnum.BUCKET.equals(profileDiscountType) &&
                                      ((dbEntity.getLastBucketCodeLoad()==null &&
                                        discountEntity.getLastBucketCodeLoadUid()!=null) ||
                                       !dbEntity.getLastBucketCodeLoad()
                                                .getUid()
                                                .equals(discountEntity.getLastBucketCodeLoadUid()));

        if (isChangedBucketLoad && dbEntity.getLastBucketCodeLoad()!=null &&
            bucketService.isLastBucketLoadStillLoading(dbEntity.getLastBucketCodeLoad().getId())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_UPDATE_DISCOUNT_BUCKET_WHILE_PROCESSING_IS_RUNNING.getValue());
        }

        if (DiscountStateEnum.PUBLISHED.equals(dbEntity.getState()) &&
            DiscountCodeTypeEnum.LANDINGPAGE.equals(profileDiscountType) &&
            (!dbEntity.getLandingPageUrl().equals(discountEntity.getLandingPageUrl()) ||
             !dbEntity.getLandingPageReferrer().equals(discountEntity.getLandingPageReferrer()))) {
            dbEntity.setState(DiscountStateEnum.DRAFT);
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

        // refresh materialized views
        refreshMaterializedViews(profile);

        return new CrudDiscountWrapper(dbEntity, profileDiscountType, isChangedBucketLoad);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteDiscount(String agreementId, Long discountId) {
        // check if agreement exits. If not the method throw an exception
        agreementServiceLight.findAgreementById(agreementId);
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        discountRepository.deleteById(discountId);

        // refresh materialized views
        refreshMaterializedViews(profileEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity testDiscount(String agreementId, Long discountId) {
        AgreementEntity agreementEntity = agreementServiceLight.findAgreementById(agreementId);
        DiscountEntity discount = findDiscountById(discountId);

        validateTestingDiscount(agreementEntity, discount);

        discount.setState(DiscountStateEnum.TEST_PENDING);
        discount = discountRepository.save(discount);

        agreementServiceLight.setInformationLastUpdateDate(agreementEntity);

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
        AgreementEntity agreementEntity = agreementServiceLight.findAgreementById(agreementId);

        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId())
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        DiscountEntity discount = findDiscountById(discountId);
        // we should update start date to "now" if it's in the past
        if (LocalDate.now().isAfter(discount.getStartDate())) {
            discount.setStartDate(LocalDate.now());
        }
        validatePublishingDiscount(agreementEntity, discount, profileEntity);
        discount.setState(DiscountStateEnum.PUBLISHED);
        discount = discountRepository.save(discount);
        agreementServiceLight.setInformationLastUpdateDate(agreementEntity);
        // check if exists almost one discount already published
        if (agreementEntity.getFirstDiscountPublishingDate()==null) {
            long numPublishedDiscount = discountRepository.countByAgreementIdAndState(agreementId,
                                                                                      DiscountStateEnum.PUBLISHED);
            if (numPublishedDiscount==1) { // 1 -> discount just created
                agreementServiceLight.setFirstDiscountPublishingDate(agreementEntity);
            }
        }

        // refresh materialized views
        refreshMaterializedViews(profileEntity);

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity unpublishDiscount(String agreementId, Long discountId) {
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        DiscountEntity discount = findDiscountById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.PUBLISHED.equals(discount.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_UNPUBLISH_DISCOUNT_NOT_PUBLISHED.getValue());
        }
        discount.setState(DiscountStateEnum.DRAFT);
        discount = discountRepository.save(discount);

        // refresh materialized views
        refreshMaterializedViews(profileEntity);

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity suspendDiscount(String agreementId, Long discountId, String reasonMessage) {

        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        DiscountEntity discount = findDiscountById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (DiscountStateEnum.SUSPENDED.equals(discount.getState())) {
            return discount; // already suspended
        }
        if (!DiscountStateEnum.PUBLISHED.equals(discount.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_SUSPEND_DISCOUNT_NOT_PUBLISHED.getValue());
        }
        discount.setState(DiscountStateEnum.SUSPENDED);
        discount.setSuspendedReasonMessage(reasonMessage);
        discount = discountRepository.save(discount);

        // send notification
        emailNotificationFacade.notifyMerchantDiscountSuspended(profileEntity, discount.getName(), reasonMessage);

        // refresh materialized views
        refreshMaterializedViews(profileEntity);

        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public String getDiscountBucketCode(String agreementId, Long discountId) {
        DiscountEntity discount = findDiscountById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.TEST_PENDING.equals(discount.getState()) &&
            !DiscountStateEnum.PUBLISHED.equals(discount.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_GET_BUCKET_CODE_FOR_DISCOUNT_NOT_TEST_PENDING_OR_NOT_PUBLISHED.getValue());
        }

        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        if (!DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_GET_BUCKET_CODE_FOR_DISCOUNT_NO_BUCKET.getValue());
        }

        DiscountBucketCodeEntity discountBucketCodeEntity = discountBucketCodeRepository.getOneForDiscount(discountId);

        if (discountBucketCodeEntity!=null) {
            discountBucketCodeRepository.burnDiscountBucketCode(discountBucketCodeEntity.getId());
            return discountBucketCodeEntity.getCode();
        } else {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_RETRIEVE_BUCKET_CODE_FROM_DISCOUNT_WITH_EMPTY_BUCKET.getValue());
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void setDiscountTestPassed(String agreementId, Long discountId) {
        // send notification
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException((ErrorCodeEnum.PROFILE_NOT_FOUND.getValue())));
        DiscountEntity discount = findDiscountById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.TEST_PENDING.equals(discount.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_SET_DISCOUNT_STATE_FOR_DISCOUNT_NOT_IN_TEST_PENDING.getValue());
        }
        discount.setTestFailureReason(null);
        discount.setState(DiscountStateEnum.TEST_PASSED);
        discount = discountRepository.save(discount);

        emailNotificationFacade.notifyMerchantDiscountTestPassed(profileEntity, discount.getName());
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void setDiscountTestFailed(String agreementId, Long discountId, String reasonMessage) {
        // send notification
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException((ErrorCodeEnum.PROFILE_NOT_FOUND.getValue())));
        DiscountEntity discount = findDiscountById(discountId);
        checkDiscountRelatedSameAgreement(discount, agreementId);
        if (!DiscountStateEnum.TEST_PENDING.equals(discount.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_SET_DISCOUNT_STATE_FOR_DISCOUNT_NOT_IN_TEST_PENDING.getValue());
        }
        discount.setState(DiscountStateEnum.TEST_FAILED);
        discount.setTestFailureReason(reasonMessage);
        discount = discountRepository.save(discount);

        emailNotificationFacade.notifyMerchantDiscountTestFailed(profileEntity, discount.getName(), reasonMessage);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void refreshMaterializedViews(ProfileEntity profileEntity) {
        publishedProductCategoryRepository.refreshView();
        merchantRepository.refreshView();
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

        emailNotificationFacade.notifyMerchantDiscountExpiring(discount);
        discount.setExpirationWarningSentDateTime(OffsetDateTime.now());
        discountRepository.save(discount);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountBucketCodeLoadingProgess getDiscountBucketCodeLoadingProgess(String agreementId, Long discountId) {
        DiscountEntity discountEntity = getDiscountById(agreementId, discountId);
        var loadedCodes = bucketService.countLoadedCodes(discountEntity);
        var percent =
                Float.valueOf(loadedCodes) / Float.valueOf(discountEntity.getLastBucketCodeLoad().getNumberOfCodes()) *
                100;
        var progress = new DiscountBucketCodeLoadingProgess();
        progress.setLoaded(loadedCodes);
        progress.setPercent(percent);
        return progress;
    }

    public DiscountEntity findDiscountById(Long discountId) {
        return discountRepository.findById(discountId)
                                 .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.DISCOUNT_NOT_FOUND.getValue()));
    }

    public void checkDiscountRelatedSameAgreement(DiscountEntity discountEntity, String agreementId) {
        if (!agreementId.equals(discountEntity.getAgreement().getId())) {
            throw new InvalidRequestException(ErrorCodeEnum.DISCOUNT_NOT_RELATED_TO_AGREEMENT_PROVIDED.getValue());
        }
    }

    private ProfileEntity validateDiscount(String agreementId,
                                           DiscountEntity discountEntity,
                                           boolean isBucketFileChanged) {

        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        if (DiscountCodeTypeEnum.LANDINGPAGE.equals(profileEntity.getDiscountCodeType()) &&
            (discountEntity.getVisibleOnEyca() && StringUtils.isEmpty(discountEntity.getEycaLandingPageUrl()) ||
             !discountEntity.getVisibleOnEyca() && !StringUtils.isEmpty(discountEntity.getEycaLandingPageUrl()))) {
            throw new InvalidRequestException(ErrorCodeEnum.VISIBLE_ON_EYCA_NOT_CONSISTENT_WITH_URL.getValue());
        }


        commonDiscountValidation(profileEntity, discountEntity, isBucketFileChanged);

        ValidationUtils.performConstraintValidation(factory.getValidator(), discountEntity);
        return profileEntity;
    }

    private void validatePublishingDiscount(AgreementEntity agreementEntity,
                                            DiscountEntity discount,
                                            ProfileEntity profileEntity) {
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
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_WITH_ONLINE_DISCOUNT_WITH_NOT_PASSED_TEST.getValue());
        }
    }

    private void validateTestingDiscount(AgreementEntity agreementEntity, DiscountEntity discount) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId())
                                                    .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        long remainingCodes = discountBucketCodeRepository.countNotUsedByDiscountId(discount.getId());

        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType()) && remainingCodes <= 0) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_WITH_DISCOUNT_WITH_EMPTY_BUCKET.getValue());
        }

        // check sales channel
        if (SalesChannelEnum.OFFLINE.equals(profileEntity.getSalesChannel())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_TEST_DISCOUNTS_WITH_OFFLINE_MERCHANTS.getValue());
        }

        // perform common discount validation to keep entities coherent
        commonDiscountValidation(profileEntity, discount, false);

        // perform testing specific validation
        validateTestingDiscount(profileEntity, agreementEntity, discount);
    }

    private void validateTestingDiscount(ProfileEntity profileEntity,
                                         AgreementEntity agreementEntity,
                                         DiscountEntity discount) {
        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType()) &&
            (discount.getLastBucketCodeLoad()==null ||
             bucketService.isLastBucketLoadStillLoading(discount.getLastBucketCodeLoad().getId()))) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_WITH_DISCOUNT_WITH_BUCKET_LOAD_IN_PROGRESS.getValue());
        }
        if (!AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_WITH_DISCOUNT_WITH_NOT_APPROVED_AGREEMENT.getValue());
        }
        if (DiscountStateEnum.SUSPENDED.equals(discount.getState())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_WITH_SUSPENDED_DISCOUNT.getValue());
        }
        if (LocalDate.now().isAfter(discount.getEndDate())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_PROCEED_WITH_EXPIRED_DISCOUNT.getValue());
        }

        checkDiscountRelatedSameAgreement(discount, agreementEntity.getId());

        long publishedDiscount = discountRepository.countByAgreementIdAndStateAndEndDateGreaterThan(agreementEntity.getId(),
                                                                                                    DiscountStateEnum.PUBLISHED,
                                                                                                    LocalDate.now());

        if (publishedDiscount >= MAX_NUMBER_PUBLISHED_DISCOUNT) {
            throw new InvalidRequestException(ErrorCodeEnum.MAX_NUMBER_OF_PUBLISHABLE_DISCOUNTS_REACHED.getValue());
        }
    }

    private void commonDiscountValidation(ProfileEntity profileEntity,
                                          DiscountEntity discountEntity,
                                          boolean isBucketFileChanged) {

        if (discountEntity.getProducts().size() > 2) {
            throw new InvalidRequestException(ErrorCodeEnum.DISCOUNT_CANNOT_HAVE_MORE_THAN_TWO_CATEGORIES.getValue());
        }

        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType()) &&
            StringUtils.isBlank(discountEntity.getStaticCode())) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_HAVE_EMPTY_STATIC_CODE_FOR_PROFILE_WITH_STATIC_CODE.getValue());
        }

        if (DiscountCodeTypeEnum.LANDINGPAGE.equals(profileEntity.getDiscountCodeType()) &&
            (StringUtils.isBlank(discountEntity.getLandingPageUrl()) ||
             StringUtils.isBlank(discountEntity.getLandingPageReferrer()))) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_HAVE_EMPTY_LANDING_PAGE_URL_FOR_PROFILE_LANDING_PAGE.getValue());
        }

        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType()) && isBucketFileChanged &&
            (discountEntity.getLastBucketCodeLoadUid()==null ||
             !bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()))) {
            throw new InvalidRequestException(ErrorCodeEnum.DISCOUNT_CANNOT_REFERENCE_TO_MISSING_BUCKET_FILE_FOR_DISCOUNT_WITH_BUCKET.getValue());
        }


        // If profile use API, static code and landing page will not used
        if (DiscountCodeTypeEnum.API.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setEycaLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
            discountEntity.setLastBucketCodeLoadFileName(null);
            discountEntity.setLastBucketCodeLoad(null);
            bucketLoadUtils.deleteBucketCodes(discountEntity.getId());
        }

        // If profile use STATIC, landing page will not used
        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setLandingPageUrl(null);
            discountEntity.setEycaLandingPageUrl(null);
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
            discountEntity.setEycaLandingPageUrl(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
            discountEntity.setLastBucketCodeLoadFileName(null);
            discountEntity.setLastBucketCodeLoad(null);
            bucketLoadUtils.deleteBucketCodes(discountEntity.getId());
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void suspendDiscountIfDiscountBucketCodesAreExpired(DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity) {
        var discountBucketCodeSummary = discountBucketCodeSummaryRepository.getReferenceById(
                discountBucketCodeSummaryEntity.getId());
        DiscountEntity discount = discountBucketCodeSummary.getDiscount();
        suspendDiscount(discount.getAgreement().getId(), discount.getId(), "La lista di codici è esaurita.");
    }
}
