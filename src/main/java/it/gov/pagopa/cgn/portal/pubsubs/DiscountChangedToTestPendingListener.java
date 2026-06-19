package it.gov.pagopa.cgn.portal.pubsubs;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Data

@Component
@Slf4j
public class DiscountChangedToTestPendingListener {

    private final EmailNotificationFacade emailNotificationFacade;

    @Autowired
    public DiscountChangedToTestPendingListener(EmailNotificationFacade emailNotificationFacade) {
        this.emailNotificationFacade = emailNotificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DiscountChangedToTestPendingEvent event) {
        try {
            emailNotificationFacade.notifyDepartementToTestDiscount(
                    event.getAgreementId(),
                    event.getDiscountId()
            );

        } catch (Exception ex) {
            log.error(
                    "Listener error on sending TEST_PENDING notification. agreementId={}, discountId={}",
                    event.getAgreementId(),
                    event.getDiscountId(),
                    ex
            );
        }
    }
}
