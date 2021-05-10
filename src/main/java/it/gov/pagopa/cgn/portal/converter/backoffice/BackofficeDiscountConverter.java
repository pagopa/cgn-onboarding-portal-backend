package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Discount;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BackofficeDiscountConverter extends CommonBackofficeDiscountConverter<DiscountEntity, Discount> {

    @Override
    protected Function<DiscountEntity, Discount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Discount, DiscountEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<DiscountEntity, Discount> toDto =
            entity -> {
                Discount dto = new Discount();
                dto.setId(String.valueOf(entity.getId()));
                dto.setName(entity.getName());
                dto.setAgreementId(entity.getAgreement().getId());
                dto.setState(discountStateEnum.get(entity.getState()));
                return dto;
            };
}
