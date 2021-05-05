package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Discount;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DiscountState;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class BackofficeDiscountConverter extends AbstractConverter<DiscountEntity, Discount> {

    private static final Map<DiscountStateEnum, DiscountState> enumMap = new EnumMap<>(DiscountStateEnum.class);
    static {
        enumMap.put(DiscountStateEnum.DRAFT, DiscountState.DRAFT);
        enumMap.put(DiscountStateEnum.PUBLISHED, DiscountState.PUBLISHED);
        enumMap.put(DiscountStateEnum.REJECTED, DiscountState.REJECTED);
    }

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
                dto.setState(enumMap.get(entity.getState()));
                return dto;
            };
}
