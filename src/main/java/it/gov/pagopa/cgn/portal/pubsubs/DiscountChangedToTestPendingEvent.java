package it.gov.pagopa.cgn.portal.pubsubs;

import lombok.Data;

import java.util.Objects;

@Data
public class DiscountChangedToTestPendingEvent {

    private final String agreementId;
    private final Long discountId;

    public DiscountChangedToTestPendingEvent(
            String agreementId,
            Long discountId) {
        this.agreementId = Objects.requireNonNull(agreementId);
        this.discountId = Objects.requireNonNull(discountId);
    }

}
