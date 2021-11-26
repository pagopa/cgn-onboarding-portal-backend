package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import it.gov.pagopa.cgnonboardingportal.model.Discount;
import it.gov.pagopa.cgnonboardingportal.model.Discounts;
import it.gov.pagopa.cgnonboardingportal.model.UpdateDiscount;
import it.gov.pagopa.cgn.portal.converter.discount.CreateDiscountConverter;
import it.gov.pagopa.cgn.portal.converter.discount.DiscountConverter;
import it.gov.pagopa.cgn.portal.converter.discount.UpdateDiscountConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;
import it.gov.pagopa.cgn.portal.wrapper.CrudDiscountWrapper;

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
    private final BucketLoadUtils bucketLoadUtils;

    public ResponseEntity<Discount> createDiscount(String agreementId, CreateDiscount createDiscountDto) {
        DiscountEntity discountEntity = createDiscountConverter.toEntity(createDiscountDto);
        CrudDiscountWrapper wrapper = discountService.createDiscount(agreementId, discountEntity);
        discountEntity = wrapper.getDiscountEntity();
        if (DiscountCodeTypeEnum.BUCKET.equals(wrapper.getProfileDiscountCodeType())) {
            bucketLoadUtils.storeCodesBucket(discountEntity.getId());
        }
        return ResponseEntity.ok(discountConverter.toDto(discountEntity));
    }

    public ResponseEntity<Discounts> getDiscounts(String agreementId) {
        List<DiscountEntity> discountList = discountService.getDiscounts(agreementId);
        Discounts discounts = discountConverter.getDiscountsDtoFromDiscountEntityList(discountList);
        return ResponseEntity.ok(discounts);
    }

    public ResponseEntity<Discount> getDiscountById(String agreementId, String discountId) {
        DiscountEntity discountEntity = discountService.getDiscountById(agreementId, Long.valueOf(discountId));
        return ResponseEntity.ok(discountConverter.toDto(discountEntity));
    }

    public ResponseEntity<Discount> updateDiscount(String agreementId, String discountId,
            UpdateDiscount updateDiscountDto) {
        DiscountEntity discountEntity = updateDiscountConverter.toEntity(updateDiscountDto);
        CrudDiscountWrapper wrapper = discountService.updateDiscount(agreementId, Long.valueOf(discountId),
                discountEntity);
        discountEntity = wrapper.getDiscountEntity();
        if (DiscountCodeTypeEnum.BUCKET.equals(wrapper.getProfileDiscountCodeType()) && wrapper.isChangedBucketLoad()) {
            bucketLoadUtils.storeCodesBucket(discountEntity.getId());
        }
        return ResponseEntity.ok(discountConverter.toDto(discountEntity));
    }

    public void deleteDiscount(String agreementId, String discountId) {
        discountService.deleteDiscount(agreementId, Long.valueOf(discountId));
    }

    public void publishDiscount(String agreementId, String discountId) {
        discountService.publishDiscount(agreementId, Long.valueOf(discountId));
    }

    @Autowired
    public DiscountFacade(DiscountService discountService, CreateDiscountConverter createDiscountConverter,
            DiscountConverter discountConverter, UpdateDiscountConverter updateDiscountConverter,
            BucketLoadUtils bucketLoadUtils) {
        this.discountService = discountService;
        this.createDiscountConverter = createDiscountConverter;
        this.discountConverter = discountConverter;
        this.updateDiscountConverter = updateDiscountConverter;
        this.bucketLoadUtils = bucketLoadUtils;
    }

}
