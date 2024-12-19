package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailParams.Attachment;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EmailNotificationFacade {

    private final TemplateEngine htmlTemplateEngine;

    private final EmailNotificationService emailNotificationService;

    private final ConfigProperties configProperties;

    private static final String CONTEXT_DISCOUNT_NAME = "discount_name";  // Compliant


    public void notifyDepartmentNewAgreementRequest(String merchantFullName) {
        var subject = "[Carta Giovani Nazionale] Nuova richiesta di convenzione da " + merchantFullName;
        var context = new Context();
        context.setVariable("merchant_fullname", merchantFullName);
        final String errorMessage =
                "Failed to send New Agreement Request notification from " + merchantFullName + " to department";
        String body = getTemplateHtml(TemplateEmail.NEW_AGREEMENT, context);
        var emailParams = createEmailParams(configProperties.getCgnDepartmentEmail(), subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyDepartementToTestDiscount(String merchantFullName, String discountName, String discountType) {
        var subject = "[Carta Giovani Nazionale] Nuova richiesta di test convenzione da " + merchantFullName;
        var context = new Context();
        context.setVariable("operator_name", merchantFullName);
        context.setVariable(CONTEXT_DISCOUNT_NAME, discountName);
        context.setVariable("discount_type", discountType);
        final String errorMessage =
                "Failed to send test request notification from " + merchantFullName + " to department";
        String body = getTemplateHtml(TemplateEmail.DISCOUNT_TEST_REQUEST, context);
        var emailParams = createEmailParams(configProperties.getCgnDepartmentEmail(), subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyMerchantAgreementRequestApproved(ProfileEntity profile,
                                                       SalesChannelEnum salesChannel,
                                                       Optional<DiscountCodeTypeEnum> discountCodeTypeOpt) {
        var subject = "[Carta Giovani Nazionale] Richiesta di convenzione approvata";

        var referentEmail = profile.getReferent().getEmailAddress();

        final String errorMessage = "Failed to send Agreement Request Approved notification to: " + referentEmail;
        try {
            TemplateEmail template = getApprovedAgreementTemplateBySalesChannel(salesChannel, discountCodeTypeOpt);

            var secondaryReferents = retrieveSecondaryRecipients(profile);

            String body = getTemplateHtml(template);
            var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
            emailNotificationService.sendAsyncMessage(emailParams);
        } catch (Exception e) {
            log.error(errorMessage, e);
        }
    }

    private TemplateEmail getApprovedAgreementTemplateBySalesChannel(SalesChannelEnum salesChannel,
                                                                     Optional<DiscountCodeTypeEnum> discountCodeTypeOpt) {
        switch (salesChannel) {
            case BOTH:
                return TemplateEmail.APPROVED_AGREEMENT_BOTH;
            case OFFLINE:
                return TemplateEmail.APPROVED_AGREEMENT_OFFLINE;
            case ONLINE:
                return getApprovedAgreementTemplateByDiscountCodeType(discountCodeTypeOpt.orElseThrow(() -> new InvalidValueException(
                        "An online merchant must have a Discount Code validation type set")));
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

    public void notifyMerchantAgreementRequestRejected(ProfileEntity profile, String rejectionMessage) {
        var subject = "[Carta Giovani Nazionale] Richiesta di convenzione rifiutata";
        var context = new Context();
        context.setVariable("rejection_message", rejectionMessage);
        var referentEmail = profile.getReferent().getEmailAddress();

        final String errorMessage = "Failed to send Agreement Request Rejected notification to: " + referentEmail;

        var secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.REJECTED_AGREEMENT, context);
        var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyDepartmentNewHelpRequest(HelpRequestParams helpRequestParams)
            throws MessagingException {
        var subject =
                "[Carta Giovani Nazionale] Nuova richiesta di supporto da " + helpRequestParams.getMerchantLegalName();
        var context = new Context();

        var categoryAndTopic = helpRequestParams.getTopic()
                                                .filter(s -> !s.isBlank())
                                                .map(topic -> helpRequestParams.getHelpCategory() + ", " + topic)
                                                .orElseGet(helpRequestParams::getHelpCategory);

        context.setVariable("help_category_and_topic", categoryAndTopic);
        context.setVariable("help_message", helpRequestParams.getMessage());
        context.setVariable("merchant_legal_name", helpRequestParams.getMerchantLegalName());
        context.setVariable("referent_first_name", helpRequestParams.getReferentFirstName());
        context.setVariable("referent_last_name", helpRequestParams.getReferentLastName());

        var body = getTemplateHtml(TemplateEmail.HELP_REQUEST, context);
        var emailParams = createEmailParams(configProperties.getCgnDepartmentEmail(),
                                            helpRequestParams.getReplyToEmailAddress(),
                                            subject,
                                            body,
                                            null);
        emailNotificationService.sendSyncMessage(emailParams);
    }

    public void notifyMerchantDiscountSuspended(ProfileEntity profile, String discountName, String suspensionMessage) {
        var subject = "[Carta Giovani Nazionale] Agevolazione sospesa";
        var context = new Context();
        context.setVariable(CONTEXT_DISCOUNT_NAME, discountName);
        context.setVariable("suspension_message", suspensionMessage);
        var referentEmail = profile.getReferent().getEmailAddress();

        final String errorMessage = "Failed to send Discount Suspended notification to: " + referentEmail;

        var secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.SUSPENDED_DISCOUNT, context);
        var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyMerchantDiscountTestPassed(ProfileEntity profile, String discountName) {
        var subject = "[Carta Giovani Nazionale] Il test è stato superato";
        var context = new Context();
        context.setVariable(CONTEXT_DISCOUNT_NAME, discountName);
        var referentEmail = profile.getReferent().getEmailAddress();
        final String errorMessage = "Failed to send Discount Test Passed notification to: " + referentEmail;

        var secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.DISCOUNT_TEST_PASSED, context);
        var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyMerchantDiscountTestFailed(ProfileEntity profile, String discountName, String reasonMessage) {
        var subject = "[Carta Giovani Nazionale] Il test non è stato superato";
        var context = new Context();
        context.setVariable(CONTEXT_DISCOUNT_NAME, discountName);
        context.setVariable("suspension_message", reasonMessage);
        var referentEmail = profile.getReferent().getEmailAddress();
        final String errorMessage = "Failed to send Discount Test Failed notification to: " + referentEmail;

        var secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.DISCOUNT_TEST_FAILED, context);
        var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyMerchantDiscountExpiring(DiscountEntity discount) {
        var subject = "[Carta Giovani Nazionale] La tua agevolazione sta per scadere";
        var context = new Context();
        context.setVariable(CONTEXT_DISCOUNT_NAME, discount.getName());

        ProfileEntity profileEntity = discount.getAgreement().getProfile();
        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        try {
            var body = getTemplateHtml(TemplateEmail.EXPIRED_DISCOUNT, context);
            var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, null);
            emailNotificationService.sendSyncMessage(emailParams);
        } catch (Exception e) {
            // in this case exception will be propagated
            throw new CGNException(e);
        }
    }

    public static String createTrackingKeyForExpirationNotification(DiscountEntity discount,
                                                                    BucketCodeExpiringThresholdEnum threshold) {
        return threshold.name() + "::" + discount.getId() + "::" + discount.getLastBucketCodeLoad().getUid();
    }

    public void notifyMerchantDiscountBucketCodesExpiring(DiscountEntity discount,
                                                          BucketCodeExpiringThresholdEnum threshold,
                                                          Long remainingCodes) {
        var subject = "[Carta Giovani Nazionale] La lista di codici sconto per la tua agevolazione sta per esaurirsi";
        var context = new Context();

        ProfileEntity profileEntity = discount.getAgreement().getProfile();
        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        context.setVariable(CONTEXT_DISCOUNT_NAME, discount.getName());
        context.setVariable("missing_codes", remainingCodes);
        final String errorMessage = "Failed to send Discount Bucket Codes Expiring notification to: " + referentEmail;
        final String trackingKey = createTrackingKeyForExpirationNotification(discount, threshold);

        var body = getTemplateHtml(TemplateEmail.EXPIRING_BUCKET_CODES, context);
        var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams, trackingKey);
    }

    public void notifyMerchantDiscountBucketCodesExpired(DiscountEntity discount) {
        var subject = "[Carta Giovani Nazionale] La lista di codici sconto per la tua agevolazione è esaurita";
        var context = new Context();

        ProfileEntity profileEntity = discount.getAgreement().getProfile();
        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        context.setVariable(CONTEXT_DISCOUNT_NAME, discount.getName());
        final String errorMessage = "Failed to send Discount Bucket Codes Expired notification to: " + referentEmail;
        final String trackingKey = createTrackingKeyForExpirationNotification(discount,
                                                                              BucketCodeExpiringThresholdEnum.PERCENT_0);

        var body = getTemplateHtml(TemplateEmail.EXPIRED_BUCKET_CODES, context);
        var emailParams = createEmailParams(referentEmail, secondaryReferents, subject, body, errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams, trackingKey);
    }

    @Autowired
    public EmailNotificationFacade(TemplateEngine htmlTemplateEngine,
                                   EmailNotificationService emailNotificationService,
                                   ConfigProperties configProperties) {
        this.htmlTemplateEngine = htmlTemplateEngine;
        this.emailNotificationService = emailNotificationService;
        this.configProperties = configProperties;
    }

    private EmailParams createEmailParams(List<String> mailTo, String subject, String body, String failureMessage) {
        return createEmailParams(mailTo,
                                 Optional.empty(),
                                 Optional.empty(),
                                 subject,
                                 body,
                                 failureMessage,
                                 Optional.empty());
    }

    private EmailParams createEmailParams(List<String> mailTo,
                                          String subject,
                                          String body,
                                          String failureMessage,
                                          List<Attachment> attachments) {
        return createEmailParams(mailTo,
                                 Optional.empty(),
                                 Optional.empty(),
                                 subject,
                                 body,
                                 failureMessage,
                                 Optional.of(attachments));
    }

    private EmailParams createEmailParams(String mailTo, String subject, String body, String failureMessage) {
        return createEmailParams(mailTo,
                                 Optional.empty(),
                                 Optional.empty(),
                                 subject,
                                 body,
                                 failureMessage,
                                 Optional.empty());
    }

    private EmailParams createEmailParams(String mailTo,
                                          List<String> secondaryMailToList,
                                          String subject,
                                          String body,
                                          String failureMessage) {
        return createEmailParams(mailTo,
                                 Optional.of(secondaryMailToList),
                                 Optional.empty(),
                                 subject,
                                 body,
                                 failureMessage,
                                 Optional.empty());
    }

    private EmailParams createEmailParams(String mailTo,
                                          String replyToOpt,
                                          String subject,
                                          String body,
                                          String failureMessage) {
        return createEmailParams(mailTo,
                                 Optional.empty(),
                                 Optional.of(replyToOpt),
                                 subject,
                                 body,
                                 failureMessage,
                                 Optional.empty());
    }


    private EmailParams createEmailParams(List<String> mailTo,
                                          Optional<List<String>> ccList,
                                          Optional<String> replyToOpt,
                                          String subject,
                                          String body,
                                          String failureMessage,
                                          Optional<List<Attachment>> attachments) {
        return EmailParams.builder()
                          .mailFrom(configProperties.getCgnNotificationSender())
                          .logoName("cgn-logo.png")
                          .logo(configProperties.getCgnLogo())
                          .mailToList(mailTo)
                          .mailCCList(ccList)
                          .replyToOpt(replyToOpt)
                          .subject(subject)
                          .body(body)
                          .failureMessage(failureMessage)
                          .attachments(attachments)
                          .build();
    }

    private EmailParams createEmailParams(String mailTo,
                                          Optional<List<String>> ccList,
                                          Optional<String> replyToOpt,
                                          String subject,
                                          String body,
                                          String failureMessage,
                                          Optional<List<Attachment>> attachments) {
        return createEmailParams(Collections.singletonList(mailTo),
                                 ccList,
                                 replyToOpt,
                                 subject,
                                 body,
                                 failureMessage,
                                 attachments);
    }

    public void notifyAdminForJobEyca(List<Attachment> attachments, String body) {
        String subject = "Eyca job launch summary attachments of: " +
                         LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String failureMessage = "It is not possible to send the email with the job summary attacchments.";
        EmailParams emailParams = createEmailParams(Arrays.asList(configProperties.getEycaJobMailTo().split(";")),
                                                    subject,
                                                    body,
                                                    failureMessage,
                                                    attachments);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    public void notifyEycaAdmin(String body) {
        String subject = "Discounts for Generic Code/URLs " +
                         LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH));
        String failureMessage = "It is not possible to send the email to Eyca admin";
        EmailParams emailParams = createEmailParams(Arrays.asList(configProperties.getEycaAdminMailTo().split(";")),
                                                    subject,
                                                    body,
                                                    failureMessage);
        emailNotificationService.sendAsyncMessage(emailParams);
    }

    private String getTemplateHtml(TemplateEmail template) {
        return getTemplateHtml(template, new Context());
    }

    private String getTemplateHtml(TemplateEmail template, Context context) {
        return htmlTemplateEngine.process(template.getTemplateName(), context);
    }

    private List<String> retrieveSecondaryRecipients(ProfileEntity profileEntity) {
        List<String> secondaryReferents = Optional.ofNullable(profileEntity.getSecondaryReferentList())
                                                  .orElse(Collections.emptyList())
                                                  .stream()
                                                  .map(SecondaryReferentEntity::getEmailAddress)
                                                  .collect(Collectors.toList());
        secondaryReferents.addAll(Collections.singletonList(configProperties.getCgnDepartmentEmail()));

        return secondaryReferents;
    }

    private static class InvalidValueException
            extends RuntimeException {
        public InvalidValueException(String message) {
            super(message);
        }
    }

}