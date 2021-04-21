package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import it.gov.pagopa.cgnonboardingportal.model.Discount;
import it.gov.pagopa.cgnonboardingportal.model.Discounts;
import it.gov.pagopa.cgnonboardingportal.model.UpdateDiscount;
import it.gov.pagopa.cgn.portal.converter.discount.CreateDiscountConverter;
import it.gov.pagopa.cgn.portal.converter.discount.DiscountConverter;
import it.gov.pagopa.cgn.portal.converter.discount.UpdateDiscountConverter;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiscountFacade {

    private final DiscountService discountService;
    private final CreateDiscountConverter createDiscountConverter;
    private final DiscountConverter discountConverter;
    private final UpdateDiscountConverter updateDiscountConverter;

    public ResponseEntity<Discount> createDiscount(String agreementId, CreateDiscount createDiscountDto) {
        DiscountEntity discountEntity = createDiscountConverter.toEntity(createDiscountDto);
        discountEntity = discountService.createDiscount(agreementId, discountEntity);
        return ResponseEntity.ok(discountConverter.toDto(discountEntity));
    }

    public ResponseEntity<Discounts> getDiscounts(String agreementId) {
        List<DiscountEntity> discountList = discountService.getDiscounts(agreementId);
        Discounts discounts = discountConverter.getDiscountsDtoFromDiscountEntityList(discountList);
        return ResponseEntity.ok(discounts);
    }

    public ResponseEntity<Discount> updateDiscount(String agreementId, String discountId, UpdateDiscount updateDiscountDto) {
        DiscountEntity discountEntity = updateDiscountConverter.toEntity(updateDiscountDto);
        discountEntity = discountService.updateDiscount(agreementId, Long.valueOf(discountId), discountEntity);
        return ResponseEntity.ok(discountConverter.toDto(discountEntity));
    }

    public void deleteDiscount(String agreementId, String discountId) {
        discountService.deleteDiscount(agreementId, Long.valueOf(discountId));
    }

    @Autowired
    public DiscountFacade(DiscountService discountService, CreateDiscountConverter createDiscountConverter,
                          DiscountConverter discountConverter, UpdateDiscountConverter updateDiscountConverter) {
        this.discountService = discountService;
        this.createDiscountConverter = createDiscountConverter;
        this.discountConverter = discountConverter;
        this.updateDiscountConverter = updateDiscountConverter;
    }
}
