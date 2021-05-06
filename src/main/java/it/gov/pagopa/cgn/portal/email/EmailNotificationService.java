package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;

@Service
public class EmailNotificationService {


    private final Logger logger = LogManager.getLogger(EmailNotificationService.class);

    private final JavaMailSender javaMailSender;

    private final TemplateEngine htmlTemplateEngine;

    private final ConfigProperties configProperties;

    @Autowired
    public EmailNotificationService(JavaMailSender javaMailSender, TemplateEngine htmlTemplateEngine, ConfigProperties configProperties) {
        this.javaMailSender = javaMailSender;
        this.htmlTemplateEngine = htmlTemplateEngine;
        this.configProperties = configProperties;
    }


    private void sendMessage(String recipient, String subject, String template, Context context, Optional<String> replyTo)
            throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(configProperties.getCgnNotificationSender());
        helper.setTo(recipient);

        if (replyTo.isPresent()) {
            helper.setReplyTo(replyTo.get());
        }

        helper.setSubject(subject);
        helper.setText(htmlTemplateEngine.process(template, context), true);

        helper.addInline("cgn-logo.png", configProperties.getCgnLogo());

        logger.info("Sending email with subject '{}', to '{}'", subject, recipient);

        javaMailSender.send(mimeMessage);
    }

    private void sendMessage(String recipient, String subject, String template, Context context) throws MessagingException {
        sendMessage(recipient, subject, template, context, Optional.empty());
    }

    private void sendMessage(String recipient, String subject, String template) throws MessagingException {
        sendMessage(recipient, subject, template, new Context());
    }


    public void notifyDepartmentNewAgreementRequest(String merchantFullName) {
        String subject = "[Carta Giovani Nazionale] Nuova richiesta di convenzione da " + merchantFullName;
        Context context = new Context();
        context.setVariable("merchant_fullname", merchantFullName);

        try {
            sendMessage(configProperties.getCgnDepartmentEmail(), subject, "email/agreement-request-new.html", context);
        } catch (Exception e) {
            logger.error("Failed to send New Agreement Request notification from " + merchantFullName + " to department", e);
        }
    }

    public void notifyMerchantAgreementRequestApproved(String referentEmail) {
        String subject = "[Carta Giovani Nazionale] Richiesta di convenzione approvata";

        try {
            sendMessage(referentEmail, subject, "email/agreement-request-approved-both.html");
        } catch (Exception e) {
            logger.error("Failed to send Agreement Request Approved notification to: " + referentEmail, e);
        }
    }

    public void notifyMerchantAgreementRequestRejected(String referentEmail, String rejectionMessage) { // TODO wait template update -> recheck
        String subject = "[Carta Giovani Nazionale] Richiesta di convenzione rifiutata";
        Context context = new Context();
        context.setVariable("rejection_message", rejectionMessage);

        try {
            sendMessage(referentEmail, subject, "email/agreement-request-rejected.html", context);
        } catch (Exception e) {
            logger.error("Failed to send Agreement Request Rejected notification to: " + referentEmail, e);
        }
    }

    // TODO to be invoked by Discoun Suspension process
    public void notifyMerchantDiscountSuspended(String referentEmail, String discountName, String suspensionMessage) {
        String subject = "[Carta Giovani Nazionale] Agevolazione sospesa";
        Context context = new Context();
        context.setVariable("discount_name", discountName);
        context.setVariable("suspension_message", suspensionMessage);

        try {
            sendMessage(referentEmail, subject, "email/discount-suspended.html", context);
        } catch (Exception e) {
            logger.error("Failed to send Discount Suspended notification to: " + referentEmail, e);
        }
    }

    // TODO expiration checking process to be implemented
    public void notifyMerchantDiscountExpiring(String referentEmail, String discountName) {
        String subject = "[Carta Giovani Nazionale] La tua agevolazione sta per scadere";
        Context context = new Context();
        context.setVariable("discount_name", discountName);

        try {
            sendMessage(referentEmail, subject, "email/discount-expiring.html", context);
        } catch (Exception e) {
            logger.error("Failed to send Discount Expiring notification to: " + referentEmail, e);
        }
    }

}
