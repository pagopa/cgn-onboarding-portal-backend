package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.Optional;

@Component
@Slf4j
public class EmailNotificationFacade {

    private final TemplateEngine htmlTemplateEngine;

    private final EmailNotificationService emailNotificationService;

    private final ConfigProperties configProperties;


    public void notifyDepartmentNewAgreementRequest(String merchantFullName) {
        var subject = "[Carta Giovani Nazionale] Nuova richiesta di convenzione da " + merchantFullName;
        var context = new Context();
        context.setVariable("merchant_fullname", merchantFullName);
        try {
            String body = getTemplateHtml(TemplateEmail.NEW_AGREEMENT, context);
            var emailParams = createEmailParams(configProperties.getCgnDepartmentEmail(), subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send New Agreement Request notification from " + merchantFullName + " to department", e);
        }
    }

    public void notifyMerchantAgreementRequestApproved(String referentEmail, SalesChannelEnum salesChannel, Optional<DiscountCodeTypeEnum> discountCodeTypeOpt) {
        var subject = "[Carta Giovani Nazionale] Richiesta di convenzione approvata";

        try {
            TemplateEmail template = getApprovedAgreementTemplateBySalesChannel(salesChannel, discountCodeTypeOpt);
            String body = getTemplateHtml(template);
            var emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Agreement Request Approved notification to: " + referentEmail, e);
        }
    }

    private TemplateEmail getApprovedAgreementTemplateBySalesChannel(SalesChannelEnum salesChannel, Optional<DiscountCodeTypeEnum> discountCodeTypeOpt) {
        switch (salesChannel) {
            case BOTH:
                return TemplateEmail.APPROVED_AGREEMENT_BOTH;
            case OFFLINE:
                return TemplateEmail.APPROVED_AGREEMENT_OFFLINE;
            case ONLINE:
                return getApprovedAgreementTemplateByDiscountCodeType(discountCodeTypeOpt
                        .orElseThrow(() -> new InvalidValueException("An online merchant must have a Discount Code validation type set")));
            default:
                throw new InvalidValueException(salesChannel + " is not a valid Sales Channel");
        }
    }

    private TemplateEmail getApprovedAgreementTemplateByDiscountCodeType(DiscountCodeTypeEnum discountCodeType) {
        switch (discountCodeType) {
            case API:
                return TemplateEmail.APPROVED_AGREEMENT_ONLINE_API_CODE;
            case STATIC:
                return TemplateEmail.APPROVED_AGREEMENT_ONLINE_STATIC_CODE;
            default:
                throw new InvalidValueException(discountCodeType + " is not a valid Discount Code Type");
        }
    }

    public void notifyMerchantAgreementRequestRejected(String referentEmail, String rejectionMessage) {
        var subject = "[Carta Giovani Nazionale] Richiesta di convenzione rifiutata";
        var context = new Context();
        context.setVariable("rejection_message", rejectionMessage);

        try {
            var body = getTemplateHtml(TemplateEmail.REJECTED_AGREEMENT, context);
            var emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Agreement Request Rejected notification to: " + referentEmail, e);
        }
    }

    public void notifyDepartmentNewHelpRequest(HelpRequestParams helpRequestParams) throws MessagingException {
        var subject = "[Carta Giovani Nazionale] Nuova richiesta di supporto da " + helpRequestParams.getMerchantLegalName();
        var context = new Context();

        var categoryAndTopic = helpRequestParams.getTopic().filter(s -> !s.isBlank())
                .map(topic -> helpRequestParams.getHelpCategory() + ", " + topic)
                .orElseGet(helpRequestParams::getHelpCategory);

        context.setVariable("help_category_and_topic", categoryAndTopic);
        context.setVariable("help_message", helpRequestParams.getMessage());

        context.setVariable("merchant_legal_name", helpRequestParams.getMerchantLegalName());
        context.setVariable("referent_first_name", helpRequestParams.getReferentFirstName());
        context.setVariable("referent_last_name", helpRequestParams.getReferentLastName());

        var body = getTemplateHtml(TemplateEmail.HELP_REQUEST, context);
        var emailParams = createEmailParams(configProperties.getCgnDepartmentEmail(), Optional.of(helpRequestParams.getReplyToEmailAddress()), subject, body);
        emailNotificationService.sendMessage(emailParams);
    }

    public void notifyMerchantDiscountSuspended(String referentEmail, String discountName, String suspensionMessage) {
        var subject = "[Carta Giovani Nazionale] Agevolazione sospesa";
        var context = new Context();
        context.setVariable("discount_name", discountName);
        context.setVariable("suspension_message", suspensionMessage);

        try {
            var body = getTemplateHtml(TemplateEmail.SUSPENDED_DISCOUNT, context);
            var emailParams = createEmailParams(referentEmail, subject, body);
            emailNotificationService.sendMessage(emailParams);
        } catch (Exception e) {
            log.error("Failed to send Discount Suspended notification to: " + referentEmail, e);
        }
    }

    public void notifyMerchantDiscountExpiring(String referentEmail, String discountName) {
        var subject = "[Carta Giovani Nazionale] La tua agevolazione sta per scadere";
        var context = new Context();
        context.setVariable("discount_name", discountName);

        try {
            var body = getTemplateHtml(TemplateEmail.EXPIRED_DISCOUNT, context);
            var emailParams = createEmailParams(referentEmail, subject, body);
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

    private static class InvalidValueException extends RuntimeException {
        public InvalidValueException(String message) {
            super(message);
        }
    }

}
