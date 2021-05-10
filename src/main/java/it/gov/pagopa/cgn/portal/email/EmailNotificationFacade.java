package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.Optional;

@Component
@Slf4j
public class EmailNotificationFacade {

    private final TemplateEngine htmlTemplateEngine;

    private final EmailNotificationService emailNotificationService;

    private final ConfigProperties configProperties;


    public void notifyDepartmentNewAgreementRequest(String merchantFullName) {
        String subject = "[Carta Giovani Nazionale] Nuova richiesta di convenzione da " + merchantFullName;
        Context context = new Context();
        context.setVariable("merchant_fullname", merchantFullName);
        try {
            String body = getTemplateHtml(TemplateEmail.NEW_AGREEMENT, context);
            EmailParams emailParams = createEmailParams(configProperties.getCgnDepartmentEmail(), subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send New Agreement Request notification from " + merchantFullName + " to department", e);
        }
    }

    public void notifyMerchantAgreementRequestApproved(String referentEmail) {
        String subject = "[Carta Giovani Nazionale] Richiesta di convenzione approvata";

        try {
            String body = getTemplateHtml(TemplateEmail.APPROVED_AGREEMENT);
            EmailParams emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Agreement Request Approved notification to: " + referentEmail, e);
        }
    }

    public void notifyMerchantAgreementRequestRejected(String referentEmail, String rejectionMessage) { // TODO wait template update -> recheck
        String subject = "[Carta Giovani Nazionale] Richiesta di convenzione rifiutata";
        Context context = new Context();
        context.setVariable("rejection_message", rejectionMessage);

        try {
            String body = getTemplateHtml(TemplateEmail.REJECTED_AGREEMENT, context);
            EmailParams emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Agreement Request Rejected notification to: " + referentEmail, e);
        }
    }

    public void notifyMerchantDiscountSuspended(String referentEmail, String discountName, String suspensionMessage) {
        String subject = "[Carta Giovani Nazionale] Agevolazione sospesa";
        Context context = new Context();
        context.setVariable("discount_name", discountName);
        context.setVariable("suspension_message", suspensionMessage);

        try {
            String body = getTemplateHtml(TemplateEmail.SUSPENDED_DISCOUNT, context);
            EmailParams emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Discount Suspended notification to: " + referentEmail, e);
        }
    }

    // TODO expiration checking process to be implemented
    public void notifyMerchantDiscountExpiring(String referentEmail, String discountName) {
        String subject = "[Carta Giovani Nazionale] La tua agevolazione sta per scadere";
        Context context = new Context();
        context.setVariable("discount_name", discountName);

        try {
            String body = getTemplateHtml(TemplateEmail.EXPIRED_DISCOUNT, context);
            EmailParams emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Discount Expiring notification to: " + referentEmail, e);
        }
    }

    @Autowired
    public EmailNotificationFacade(TemplateEngine htmlTemplateEngine, EmailNotificationService emailNotificationService,
                                   ConfigProperties configProperties) {
        this.htmlTemplateEngine = htmlTemplateEngine;
        this.emailNotificationService = emailNotificationService;
        this.configProperties = configProperties;
    }

    private EmailParams createEmailParams(String mailTo, String subject, String body) {
        return createEmailParams(mailTo, Optional.empty(), subject, body);
    }

    private EmailParams createEmailParams(String mailTo, Optional<String> replyToOpt, String subject, String body) {
        return EmailParams.builder()
                .mailFrom(configProperties.getCgnNotificationSender())
                .logoName("cgn-logo.png")
                .logo(configProperties.getCgnLogo())
                .mailToList(Collections.singletonList(mailTo))
                .replyToOpt(replyToOpt)
                .subject(subject)
                .body(body).build();
    }

    private String getTemplateHtml(TemplateEmail template) {
        return getTemplateHtml(template, new Context());
    }

    private String getTemplateHtml(TemplateEmail template, Context context) {
        return htmlTemplateEngine.process(template.getTemplateName(), context);
    }


}
