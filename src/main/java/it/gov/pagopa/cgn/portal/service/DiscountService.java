package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.LocalDate;
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


    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity createDiscount(String agreementId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        AgreementEntity agreement = agreementServiceLight.findById(agreementId);
        discountEntity.setAgreement(agreement);
        validateDiscount(agreementId, discountEntity);
        return discountRepository.save(discountEntity);
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
    public DiscountEntity updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        // check if agreement exits. If not the method throw an exception
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);

        DiscountEntity dbEntity = findById(discountId);
        checkDiscountRelatedSameAgreement(dbEntity, agreementId);
        updateConsumer.accept(discountEntity, dbEntity);
        validateDiscount(agreementId, dbEntity);
        // if state is Published, last modify must be updated because public information was modified
        if (DiscountStateEnum.PUBLISHED.equals(dbEntity.getState())) {
            agreementServiceLight.setInformationLastUpdateDate(agreementEntity);
        }
       return discountRepository.save(dbEntity);
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
            long numPublishedDiscount = discountRepository.countByAgreementIdAndState(
                    agreementId, DiscountStateEnum.PUBLISHED);
            if (numPublishedDiscount == 1) {    //1 -> discount just created
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
        //send notification
        ProfileEntity profileEntity = profileService.getProfile(agreementId).orElseThrow();
        emailNotificationFacade.notifyMerchantDiscountSuspended(profileEntity.getReferent().getEmailAddress(),
                discount.getName(), reasonMessage);
        return discount;
    }

    @Autowired
    public DiscountService(AgreementServiceLight agreementServiceLight, DiscountRepository discountRepository,
                           ProfileService profileService, EmailNotificationFacade emailNotificationFacade) {
        this.discountRepository = discountRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.profileService = profileService;
        this.emailNotificationFacade = emailNotificationFacade;
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

    private void validateDiscount(String agreementId, DiscountEntity discountEntity) {
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Cannot create discount without a profile"));
        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType()) &&
                StringUtils.isBlank(discountEntity.getStaticCode())) {
            throw new InvalidRequestException(
                    "Discount cannot have empty static code for a profile with discount code type static");
        }
        // If profile use API, static code will not used
        if (DiscountCodeTypeEnum.API.equals(profileEntity.getDiscountCodeType())) {
            discountEntity.setStaticCode(null);
        }
    }

    private void validatePublishingDiscount(AgreementEntity agreementEntity, DiscountEntity discount) {
        if (!AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            throw new InvalidRequestException("Cannot publish a discount with a not approved agreement");
        }
        if (!isContainsToday(agreementEntity.getStartDate(), agreementEntity.getEndDate())) {
            throw new InvalidRequestException("Cannot publish a discount because the agreement is expired");
        }

        if (!isContainsToday(discount.getStartDate(), discount.getEndDate())) {
            throw new InvalidRequestException("Cannot publish a discount because the discount doesn't include today's date");
        }
        checkDiscountRelatedSameAgreement(discount, agreementEntity.getId());
        long publishedDiscount = discountRepository.countByAgreementIdAndState(
                agreementEntity.getId(), DiscountStateEnum.PUBLISHED);
        if (publishedDiscount == MAX_NUMBER_PUBLISHED_DISCOUNT) {
            throw new InvalidRequestException(
                    "Cannot publish the discount because there are already " + MAX_NUMBER_PUBLISHED_DISCOUNT +
                            " public ones");
        }
    }

    private final BiConsumer<DiscountEntity, List<DiscountProductEntity>> updateProducts = (discountEntity, productsToUpdate) -> {
        //add all products from DTO. If there are products already present will be skipped
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
    };

    private boolean isContainsToday(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        return (!now.isBefore(startDate)) && (now.isBefore(endDate));
    }

}
