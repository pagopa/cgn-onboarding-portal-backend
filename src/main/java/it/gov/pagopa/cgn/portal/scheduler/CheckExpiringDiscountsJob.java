package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;

@Component
@Slf4j
public class CheckExpiringDiscountsJob implements Job {

    private final EmailNotificationFacade emailNotificationFacade;
    private final DiscountRepository discountRepository;


    @Autowired
    public CheckExpiringDiscountsJob(EmailNotificationFacade emailNotificationFacade, DiscountRepository discountRepository) {
        this.emailNotificationFacade = emailNotificationFacade;
        this.discountRepository = discountRepository;
    }

    public void execute(JobExecutionContext context) {
        log.info("Expiring Discounts Notification Job: started");

        discountRepository.findUnnotifiedExpiringDiscounts().forEach(this::processDiscount);

        log.info("Expiring Discounts Notification Job: ended");
    }

    @Transactional
    protected void processDiscount(DiscountEntity discount) {
        String referentEmailAddress = discount.getAgreement().getProfile().getReferent().getEmailAddress();

        emailNotificationFacade.notifyMerchantDiscountExpiring(referentEmailAddress, discount.getName());

        discount.setExpiration15DaysWarning(OffsetDateTime.now());
        discountRepository.save(discount);
    }
}
