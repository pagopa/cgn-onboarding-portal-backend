package it.gov.pagopa.cgn.portal.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.ValidatorFactory;

import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.exception.ConflictErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.util.ValidationUtils;
import it.gov.pagopa.cgn.portal.wrapper.CrudDiscountWrapper;

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

    @Transactional(Transactional.TxType.REQUIRED)
    public CrudDiscountWrapper createDiscount(String agreementId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        AgreementEntity agreement = agreementServiceLight.findById(agreementId);
        discountEntity.setAgreement(agreement);
        ProfileEntity profileEntity = validateDiscount(agreementId, discountEntity, true);
        DiscountEntity toReturn = discountRepository.save(discountEntity);
        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())) {
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
        if (discountEntityOptional.isEmpty()
                || !agreementId.equals(discountEntityOptional.get().getAgreement().getId())) {
            throw new InvalidRequestException("Discount not found or agreement is invalid");
        }
        return discountEntityOptional.get();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public CrudDiscountWrapper updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        var agreementEntity = agreementServiceLight.findById(agreementId);

        DiscountEntity dbEntity = findById(discountId);
        checkDiscountRelatedSameAgreement(dbEntity, agreementId);
        DiscountCodeTypeEnum profileDiscountType = profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Cannot create discount without a profile"))
                .getDiscountCodeType();

        boolean isChangedBucketLoad = profileDiscountType.equals(DiscountCodeTypeEnum.BUCKET)
                && !dbEntity.getLastBucketCodeLoad().getUid().equals(discountEntity.getLastBucketCodeLoadUid());

        if (isChangedBucketLoad && !bucketService.isLastBucketLoadTerminated(dbEntity.getLastBucketCodeLoad().getId())) {
            throw new ConflictErrorException(
                    "Cannot update discount bucket while another bucket processing is running");
        }

        updateConsumer.accept(discountEntity, dbEntity);
        validateDiscount(agreementId, dbEntity, isChangedBucketLoad);

        if (isChangedBucketLoad) {
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
            agreementServiceLight.setDraftAgreementFromRejected(agreementEntity);
            documentService.resetAllDocuments(agreementId);
        }

        discountRepository.save(dbEntity);
        return new CrudDiscountWrapper(dbEntity, profileDiscountType, isChangedBucketLoad);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteDiscount(String agreementId, Long discountId) {
        // check if agreement exits. If not the method throw an exception
        agreementServiceLight.findById(agreementId);
        discountRepository.deleteById(discountId);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity publishDiscount(String agreementId, Long discountId) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        DiscountEntity discount = findById(discountId);
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
                discount.getName(), reasonMessage);
        return discount;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void sendNotificationDiscountExpiring(DiscountEntity discount) {
        String referentEmailAddress = discount.getAgreement().getProfile().getReferent().getEmailAddress();

        emailNotificationFacade.notifyMerchantDiscountExpiring(referentEmailAddress, discount.getName());
        discount.setExpirationWarningSentDateTime(OffsetDateTime.now());
        discountRepository.save(discount);
    }

    @Autowired
    public DiscountService(DiscountRepository discountRepository, AgreementServiceLight agreementServiceLight,
                           ProfileService profileService, EmailNotificationFacade emailNotificationFacade,
                           DocumentService documentService, ValidatorFactory factory, BucketService bucketService) {
        this.discountRepository = discountRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.profileService = profileService;
        this.emailNotificationFacade = emailNotificationFacade;
        this.documentService = documentService;
        this.factory = factory;
        this.bucketService = bucketService;
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

    private ProfileEntity validateDiscount(String agreementId, DiscountEntity discountEntity, boolean isBucketFileChanged) {
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Cannot create discount without a profile"));

        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType())
                && StringUtils.isBlank(discountEntity.getStaticCode())) {
            throw new InvalidRequestException(
                    "Discount cannot have empty static code for a profile with discount code type static");
        }

        if (DiscountCodeTypeEnum.LANDINGPAGE.equals(profileEntity.getDiscountCodeType())
                && (StringUtils.isBlank(discountEntity.getLandingPageUrl())
                || StringUtils.isBlank(discountEntity.getLandingPageReferrer()))) {
            throw new InvalidRequestException(
                    "Discount cannot have empty landing page values for a profile with discount code type landingpage");
        }

        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())
                && isBucketFileChanged
                && (discountEntity.getLastBucketCodeLoadUid() == null
                || !bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()))) {
            throw new InvalidRequestException(
                    "Discount cannot reference to empty or not existing bucket file for a profile with discount code type bucket");
        }

        // If profile use API, static code and landing page will not used
        if (DiscountCodeTypeEnum.API.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
        }

        // If profile use STATIC, landing page will not used
        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
            discountEntity.setLastBucketCodeLoadUid(null);
        }

        // If profile use LANDINGPAGE, static code will not used
        if (DiscountCodeTypeEnum.LANDINGPAGE.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLastBucketCodeLoadUid(null);
        }

        // If profile use BUCKET, others will not used
        if (DiscountCodeTypeEnum.BUCKET.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
            discountEntity.setLandingPageUrl(null);
            discountEntity.setLandingPageReferrer(null);
        }

        // If profile sales channel is OFFLINE, any discount is visible on eyca
        if (SalesChannelEnum.OFFLINE.equals(profileEntity.getSalesChannel())) {
            discountEntity.setVisibleOnEyca(true);
        }

        ValidationUtils.performConstraintValidation(factory.getValidator(), discountEntity);
        return profileEntity;
    }

    private void validatePublishingDiscount(AgreementEntity agreementEntity, DiscountEntity discount) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId())
                .orElseThrow(() -> new InvalidRequestException("Cannot get discount's profile"));

        if (profileEntity.getDiscountCodeType().equals(DiscountCodeTypeEnum.BUCKET)
                && !bucketService.isLastBucketLoadTerminated(discount.getLastBucketCodeLoad().getId())) {
            throw new ConflictErrorException("Cannot publish a discount with a bucket load in progress");
        }
        if (!AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            throw new InvalidRequestException("Cannot publish a discount with a not approved agreement");
        }
        if (DiscountStateEnum.SUSPENDED.equals(discount.getState())) {
            throw new InvalidRequestException("Cannot publish a suspended discount");
        }
        if (!isContainsToday(agreementEntity.getStartDate(), agreementEntity.getEndDate())) {
            throw new InvalidRequestException("Cannot publish a discount because the agreement is expired");
        }

        if (LocalDate.now().isAfter(discount.getEndDate())) {
            throw new InvalidRequestException("Cannot publish an expired discount");
        }
        checkDiscountRelatedSameAgreement(discount, agreementEntity.getId());
        long publishedDiscount = discountRepository.countByAgreementIdAndState(agreementEntity.getId(),
                DiscountStateEnum.PUBLISHED);
        if (publishedDiscount >= MAX_NUMBER_PUBLISHED_DISCOUNT) {
            throw new InvalidRequestException("Cannot publish the discount because there are already "
                    + MAX_NUMBER_PUBLISHED_DISCOUNT + " public ones");
        }
    }

    private final BiConsumer<DiscountEntity, List<DiscountProductEntity>> updateProducts = (discountEntity,
                                                                                            productsToUpdate) -> {
        // add all products from DTO. If there are products already present will be
        // skipped
        discountEntity.addProductList(productsToUpdate);

        // search and remove (if there are) products deleted by user
        List<DiscountProductEntity> toDeleteProduct = discountEntity.getProducts().stream()
                .filter(prodEntity -> !productsToUpdate.contains(prodEntity)).collect(Collectors.toList());

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
    };

    private boolean isContainsToday(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        return (!now.isBefore(startDate)) && (now.isBefore(endDate));
    }

}
