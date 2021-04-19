package it.gov.pagopa.service;

import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.DiscountEntity;
import it.gov.pagopa.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final AgreementService agreementService;

    public DiscountEntity createDiscount(String agreementId, DiscountEntity discountEntity) {
        AgreementEntity agreement = agreementService.findById(agreementId);
        discountEntity.setAgreement(agreement);
        agreement.setDiscountsModifiedDate(LocalDate.now());
        return discountRepository.save(discountEntity);
    }

    public List<DiscountEntity> getDiscounts(String agreementId) {
        return discountRepository.findByAgreementId(agreementId);
    }

    public DiscountEntity updateDiscount(String agreementId, Long discountId, DiscountEntity discountEntity) {
        AgreementEntity agreement = agreementService.findById(agreementId);

        DiscountEntity dbEntity = discountRepository.findById(discountId)
                .orElseThrow(() -> new InvalidRequestException("Discount not found"));
        //TODO to valuate if check on agreementId of discount is required
        updateConsumer.accept(discountEntity, dbEntity);
        agreement.setDiscountsModifiedDate(LocalDate.now());
        return discountRepository.save(dbEntity);
    }

    public void deleteDiscount(String agreementId, Long discountId) {
        agreementService.findById(agreementId);
        discountRepository.deleteById(discountId);
    }

    @Autowired
    public DiscountService(AgreementService agreementService, DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
        this.agreementService = agreementService;
    }

    private final BiConsumer<DiscountEntity, DiscountEntity> updateConsumer = (toUpdateEntity, dbEntity) -> {
        dbEntity.setName(toUpdateEntity.getName());
        dbEntity.setDescription(toUpdateEntity.getDescription());
        dbEntity.setStartDate(toUpdateEntity.getStartDate());
        dbEntity.setEndDate(toUpdateEntity.getEndDate());
        dbEntity.setDiscountValue(toUpdateEntity.getDiscountValue());
        dbEntity.setProducts(toUpdateEntity.getProducts());
        dbEntity.setCondition(toUpdateEntity.getCondition());
        dbEntity.setStaticCode(toUpdateEntity.getStaticCode());
    };

}
