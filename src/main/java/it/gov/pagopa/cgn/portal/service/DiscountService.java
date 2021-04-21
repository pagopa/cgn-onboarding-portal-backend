package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final AgreementService agreementService;
    @Autowired
    private EntityManager entityManager;

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity createDiscount(String agreementId, DiscountEntity discountEntity) {
        AgreementEntity agreement = agreementService.findById(agreementId);
        discountEntity.setAgreement(agreement);
        agreement.setDiscountsModifiedDate(LocalDate.now());
        return discountRepository.save(discountEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<DiscountEntity> getDiscounts(String agreementId) {
        return discountRepository.findByAgreementId(agreementId);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public DiscountEntity updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        AgreementEntity agreement = agreementService.findById(agreementId);

        DiscountEntity dbEntity = discountRepository.findById(discountId)
                .orElseThrow(() -> new InvalidRequestException("Discount not found"));
        //TODO to valuate if check on agreementId of discount is required
        updateConsumer.accept(discountEntity, dbEntity);
        agreement.setDiscountsModifiedDate(LocalDate.now());
        return discountRepository.save(dbEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteDiscount(String agreementId, Long discountId) {
        agreementService.findById(agreementId);
        discountRepository.deleteById(discountId);
    }

    @Autowired
    public DiscountService(AgreementService agreementService, DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
        this.agreementService = agreementService;
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
