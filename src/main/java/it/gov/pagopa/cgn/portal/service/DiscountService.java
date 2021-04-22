package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
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
import java.util.List;
import java.util.function.BiConsumer;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final AgreementServiceLight agreementServiceLight;
    private final ProfileService profileService;


    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity createDiscount(String agreementId, DiscountEntity discountEntity) {
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
    public DiscountEntity updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        agreementServiceLight.findById(agreementId);

        DiscountEntity dbEntity = discountRepository.findById(discountId)
                .orElseThrow(() -> new InvalidRequestException("Discount not found"));
        if (!agreementId.equals(dbEntity.getAgreement().getId())) {
            throw new InvalidRequestException("Discount is not related to agreement provided");
        }
        updateConsumer.accept(discountEntity, dbEntity);
        validateDiscount(agreementId, dbEntity);
        return discountRepository.save(dbEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteDiscount(String agreementId, Long discountId) {
        agreementServiceLight.findById(agreementId);
        discountRepository.deleteById(discountId);
    }

    @Autowired
    public DiscountService(AgreementServiceLight agreementServiceLight, DiscountRepository discountRepository,
                           ProfileService profileService) {
        this.discountRepository = discountRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.profileService = profileService;
    }

    private void validateDiscount(String agreementId, DiscountEntity discountEntity) {
        ProfileEntity profileEntity = profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Cannot create discount without a profile"));
        if (DiscountCodeTypeEnum.STATIC.equals(profileEntity.getDiscountCodeType()) &&
                StringUtils.isBlank(discountEntity.getStaticCode())) {
            throw new InvalidRequestException(
                    "Discount cannot have empty static code for a profile with discount code type static");
        }
    }

    private final BiConsumer<DiscountEntity, List<DiscountProductEntity>> updateProducts = (discountEntity, productsToUpdate) -> {
        if (!CollectionUtils.isEmpty(discountEntity.getProducts())) {

            discountEntity.removeAllProduct();
        }
        discountEntity.addProductList(productsToUpdate);
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

}
