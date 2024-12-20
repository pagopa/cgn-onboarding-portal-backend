package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DiscountState;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class CommonBackofficeDiscountConverter<E, D>
        extends AbstractConverter<E, D> {

    protected static final Map<DiscountStateEnum, DiscountState> discountStateEnum = new EnumMap<>(DiscountStateEnum.class);

    static {
        discountStateEnum.put(DiscountStateEnum.DRAFT, DiscountState.DRAFT);
        discountStateEnum.put(DiscountStateEnum.PUBLISHED, DiscountState.PUBLISHED);
        discountStateEnum.put(DiscountStateEnum.SUSPENDED, DiscountState.SUSPENDED);
        discountStateEnum.put(DiscountStateEnum.TEST_PENDING, DiscountState.TEST_PENDING);
        discountStateEnum.put(DiscountStateEnum.TEST_FAILED, DiscountState.TEST_FAILED);
        discountStateEnum.put(DiscountStateEnum.TEST_PASSED, DiscountState.TEST_PASSED);
    }

    protected BiFunction<DiscountStateEnum, LocalDate, DiscountState> toDtoEnum = (entityEnum, endDate) -> {
        if (!LocalDate.now().isBefore(endDate)) {
            return DiscountState.EXPIRED;
        }
        return Optional.ofNullable(discountStateEnum.get(entityEnum))
                       .orElseThrow(() -> new InvalidRequestException("Enum mapping not found for " + entityEnum));
    };
}
