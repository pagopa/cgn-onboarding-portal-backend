package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailParams.Attachment;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EmailNotificationFacade {


    private final TemplateEngine htmlTemplateEngine;

    private final EmailNotificationService emailNotificationService;

    private final ConfigProperties configProperties;

    private final ParamFacade paramFacade;

    private List<String> bccList;

    public static final String FAILURE_REASON = "failure_reason";
    public static final String OPERATOR_NAME = "operator_name";
    public static final String DISCOUNT_TYPE = "discount_type";
    public static final String DISCOUNT_NAME = "discount_name";
    public static final String MISSING_CODES = "missing_codes";
    public static final String DISCOUNTS = "discounts";
    public static final String PERCENT = "percent";

    @Autowired
    public EmailNotificationFacade(TemplateEngine htmlTemplateEngine,
                                   EmailNotificationService emailNotificationService,
                                   ConfigProperties configProperties,
                                   ParamFacade paramFacade) {
        this.htmlTemplateEngine = htmlTemplateEngine;
        this.emailNotificationService = emailNotificationService;
        this.configProperties = configProperties;
        this.paramFacade = paramFacade;
    }

    @PostConstruct
    public void init() {
        bccList = Arrays.asList(paramFacade.getEycaJobMailTo());
    }

    public void notifyDepartmentNewAgreementRequest( ProfileEntity profile) {
        String merchantFullName = profile.getFullName();
        var subject = "[Carta Giovani Nazionale] Nuova richiesta di convenzione da " + merchantFullName;
        var context = new Context();
        context.setVariable("merchant_fullname", merchantFullName);
        final String errorMessage =
                "Failed to send New Agreement Request notification from " + merchantFullName + " to department";
        String body = getTemplateHtml(TemplateEmail.NEW_AGREEMENT, context);
        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.NEW_AGREEMENT_REQUEST, profile.getAgreement().getId(), 0);

        EmailParams emailParams = createEmailParams(Collections.singletonList(configProperties.getCgnDepartmentEmail()),
                          Optional.empty(),
                          Optional.of(bccList),
                          Optional.empty(), subject, body,
                          errorMessage,
                          Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }


    public void notifyDepartementToTestDiscount(AgreementEntity agreement, DiscountEntity discount) {
        String merchantFullName = discount.getAgreement().getProfile().getFullName();
        var subject = "[Carta Giovani Nazionale] Nuova richiesta di test convenzione da " + merchantFullName;
        var context = new Context();
        context.setVariable(OPERATOR_NAME, merchantFullName);
        context.setVariable(DISCOUNT_NAME, discount.getName());
        context.setVariable(DISCOUNT_TYPE, agreement.getProfile().getDiscountCodeType().getCode());
        final String errorMessage =
                "Failed to send test request notification from " + merchantFullName + " to department";
        String body = getTemplateHtml(TemplateEmail.DISCOUNT_TEST_REQUEST, context);

        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.DISCOUNT_TEST_REQUEST, agreement.getId(), discount.getId());

        EmailParams emailParams = createEmailParams(Collections.singletonList(configProperties.getCgnDepartmentEmail()),
                                                    Optional.empty(),
                                                    Optional.of(bccList),
                                                    Optional.empty(), subject, body,
                                                    errorMessage,
                                                    Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    public void notifyMerchantAgreementRequestApproved(ProfileEntity profile,
                                                       SalesChannelEnum salesChannel,
                                                       Optional<DiscountCodeTypeEnum> discountCodeTypeOpt) {
        var subject = "[Carta Giovani Nazionale] Richiesta di convenzione approvata";

        var referentEmail = profile.getReferent().getEmailAddress();

        final String errorMessage = "Failed to send Agreement Request Approved notification to: " + referentEmail;
        try {
            TemplateEmail template = getApprovedAgreementTemplateBySalesChannel(salesChannel, discountCodeTypeOpt);
            List<String> secondaryReferents = retrieveSecondaryRecipients(profile);
            String body = getTemplateHtml(template);
            final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.AGREEMENT_REQUEST_APPROVED, profile.getAgreement().getId(), 0);

           EmailParams emailParams = createEmailParams(Collections.singletonList(referentEmail),
                              Optional.of(secondaryReferents),
                              Optional.of(bccList),
                              Optional.empty(), subject, body,
                              errorMessage,
                              Optional.empty());

            emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
        } catch (Exception e) {
            log.error(errorMessage, e);
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
        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.AGREEMENT_REQUEST_REJECTED, profile.getAgreement().getId(), 0);

        EmailParams emailParams = createEmailParams(Collections.singletonList(referentEmail),
                                                    Optional.of(secondaryReferents),
                                                    Optional.of(bccList),
                                                    Optional.empty(), subject, body,
                                                    errorMessage,
                                                    Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
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

        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.HELP_REQUEST, "0",0);

        EmailParams emailParams = createEmailParams(Collections.singletonList(configProperties.getCgnDepartmentEmail()),
                                                    Optional.empty(),
                                                    Optional.of(bccList),
                                                    Optional.of(helpRequestParams.getReplyToEmailAddress()), subject, body,
                                                    null,
                                                    Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }


    public void notifyMerchantDiscountSuspended(ProfileEntity profile, DiscountEntity discount, String suspensionMessage) {
        var subject = "[Carta Giovani Nazionale] Opportunità sospesa";
        var context = new Context();
        context.setVariable(DISCOUNT_NAME, discount.getName());
        context.setVariable("suspension_message", suspensionMessage);
        var referentEmail = profile.getReferent().getEmailAddress();

        final String errorMessage = "Failed to send Discount Suspended notification to: " + referentEmail;

        var secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.SUSPENDED_DISCOUNT, context);
        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.DISCOUNT_SUSPENDED,
                                                                              profile.getAgreement().getId(),discount.getId());

        EmailParams emailParams = createEmailParams(Collections.singletonList(referentEmail),
                                                    Optional.of(secondaryReferents),
                                                    Optional.of(bccList),
                                                    Optional.empty(), subject, body,
                                                    errorMessage,
                                                    Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    public void notifyMerchantDiscountTestPassed(ProfileEntity profile, DiscountEntity discount) {
        var subject = "[Carta Giovani Nazionale] Il test è stato superato";
        var context = new Context();
        context.setVariable(DISCOUNT_NAME, discount.getName());
        var referentEmail = profile.getReferent().getEmailAddress();
        final String errorMessage = "Failed to send Discount Test Passed notification to: " + referentEmail;

        var secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.DISCOUNT_TEST_PASSED, context);
        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.DISCOUNT_TEST_PASSED,
                                                                              profile.getAgreement().getId(),discount.getId());

        EmailParams emailParams = createEmailParams(Collections.singletonList(referentEmail),
                                                    Optional.of(secondaryReferents),
                                                    Optional.of(bccList),
                                                    Optional.empty(), subject, body,
                                                    errorMessage,
                                                    Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    public void notifyMerchantDiscountTestFailed(ProfileEntity profile, DiscountEntity discount, String reasonMessage) {
        var subject = "[Carta Giovani Nazionale] Il test è fallito";
        var context = new Context();
        context.setVariable(DISCOUNT_NAME, discount.getName());
        context.setVariable(FAILURE_REASON, reasonMessage);
        String referentEmail = profile.getReferent().getEmailAddress();
        final String errorMessage = "Failed to send Discount Test Failed notification to: " + referentEmail;

        List<String> secondaryReferents = retrieveSecondaryRecipients(profile);

        var body = getTemplateHtml(TemplateEmail.DISCOUNT_TEST_FAILED, context);
        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.DISCOUNT_TEST_FAILED,
                                                                              profile.getAgreement().getId(),discount.getId());

        EmailParams emailParams = createEmailParams(Collections.singletonList(referentEmail),
                                                    Optional.of(secondaryReferents),
                                                    Optional.of(bccList),
                                                    Optional.empty(), subject, body,
                                                    errorMessage,
                                                    Optional.empty());

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    public void notifyMerchantDiscountExpiring(DiscountEntity discount) {
        var subject = "[Carta Giovani Nazionale] La tua agevolazione sta per scadere";
        var context = new Context();
        context.setVariable(DISCOUNT_NAME, discount.getName());

        ProfileEntity profileEntity = discount.getAgreement().getProfile();
        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        try {
            var body = getTemplateHtml(TemplateEmail.EXPIRING_DISCOUNT, context);
            var emailParams = createEmailParamsForAutomatedSending(referentEmail, secondaryReferents, subject, body, null);
            final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.DISCOUNT_EXPIRING,
                                                                                  discount.getAgreement().getId(),discount.getId());

            emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
        } catch (Exception e) {
            throw new CGNException(e);
        }
    }

    public void notifyWeeklyMerchantDiscountBucketCodesSummary(ProfileEntity profileEntity,
                                                               List<Map<String, Long>> listOfDiscountsToAvailableCodes) {
        var subject = "[Carta Giovani Nazionale] Riepilogo liste codici disponibili";
        var context = new Context();

        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        List<Map<String, Object>> toDisplayList = listOfDiscountsToAvailableCodes.stream().map(map -> {
            Map.Entry<String, Long> entry = map.entrySet().iterator().next();
            return Map.<String, Object>of("name", entry.getKey(), "availableCodes", entry.getValue());
        }).toList();

        context.setVariable(DISCOUNTS, toDisplayList);


        final String errorMessage =
                "Failed to send Weekly Discount Bucket Codes Summary notification to: " + referentEmail;
        final String trackingKey = createTrackingKeyForWeeklySummaryNotification(profileEntity);

        var body = getTemplateHtml(TemplateEmail.WEEKLY_SUMMARY_BUCKET_CODES, context);
        var emailParams = createEmailParamsForAutomatedSending(referentEmail, secondaryReferents, subject, body,errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    public void notifyMerchantDiscountBucketCodesExpiring(DiscountEntity discount,
                                                          BucketCodeExpiringThresholdEnum threshold,
                                                          Long remainingCodes) {
        var subject = "[Carta Giovani Nazionale] Liste codici al " + threshold.getValue() + "%";
        var context = new Context();

        ProfileEntity profileEntity = discount.getAgreement().getProfile();
        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        context.setVariable(DISCOUNT_NAME, discount.getName());
        context.setVariable(PERCENT, threshold.getValue());
        context.setVariable(MISSING_CODES, remainingCodes);
        final String errorMessage = "Failed to send Discount Bucket Codes Expiring notification to: " + referentEmail;
        final String trackingKey = createTrackingKeyForExpirationNotification(discount, threshold);

        var body = getTemplateHtml(TemplateEmail.EXPIRING_BUCKET_CODES, context);
        var emailParams = createEmailParamsForAutomatedSending(referentEmail, secondaryReferents, subject, body,errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,"Email inviata al raggiungimento di "+ remainingCodes.toString() + " codici.");
    }

    public void notifyMerchantDiscountBucketCodesExpired(DiscountEntity discount) {
        var subject = "[Carta Giovani Nazionale] Liste codici esaurite";
        var context = new Context();

        ProfileEntity profileEntity = discount.getAgreement().getProfile();
        String referentEmail = profileEntity.getReferent().getEmailAddress();
        List<String> secondaryReferents = retrieveSecondaryRecipients(profileEntity);

        context.setVariable(DISCOUNT_NAME, discount.getName());
        final String errorMessage = "Failed to send Discount Bucket Codes Expired notification to: " + referentEmail;
        final String trackingKey = createTrackingKeyForExpirationNotification(discount,
                                                                              BucketCodeExpiringThresholdEnum.PERCENT_0);

        var body = getTemplateHtml(TemplateEmail.EXPIRED_BUCKET_CODES, context);
        var emailParams = createEmailParamsForAutomatedSending(referentEmail, secondaryReferents, subject, body,errorMessage);
        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }


    public void notifyAdminForJobEyca(List<Attachment> attachments, String body) {
        String subject = "Eyca job launch summary attachments of: " +
                         LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String failureMessage = "It is not possible to send the email with the job summary attacchments.";

        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.JOB_ADMIN_EYCA,"",0);

        EmailParams emailParams = createEmailParams(bccList,
                                                    subject,
                                                    body,
                                                    failureMessage,
                                                    attachments);

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    public void notifyEycaAdmin(String body) {
        String subject = "Discounts for Generic Code/URLs " +
                         LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
        String failureMessage = "It is not possible to send the email to Eyca admin";
        final String trackingKey = createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.ADMIN_EYCA,"",0);

        boolean suspendReferentsMailSending = Boolean.parseBoolean(paramFacade.getSuspendReferentsMailSending());

        List<String> toList  = suspendReferentsMailSending ? List.of() : Arrays.asList(paramFacade.getEycaAdminMailTo());

        EmailParams emailParams = createEmailParams(toList,
                                                    bccList,
                                                    subject,
                                                    body,
                                                    failureMessage);

        emailNotificationService.sendAsyncMessage(emailParams, trackingKey,null);
    }

    private EmailParams createEmailParamsForAutomatedSending(String referentEmail,
                                                             List<String> secondaryReferents,
                                                             String subject,
                                                             String body,
                                                             String errorMessage) {

        List<String> bccList = Arrays.asList(paramFacade.getEycaJobMailTo());

        boolean suspendReferentsMailSending = Boolean.parseBoolean(paramFacade.getSuspendReferentsMailSending());

        List<String> toList  = suspendReferentsMailSending ? List.of() : Collections.singletonList(referentEmail);
        Optional<List<String>> ccList = suspendReferentsMailSending ? Optional.empty() : Optional.of(secondaryReferents);

        return  createEmailParams(toList,
                                  ccList,
                                  Optional.of(bccList),
                                  Optional.empty(), subject, body,
                                  errorMessage,
                                  Optional.empty());
    }

    public static String createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum emailEvent, String agreementId, long discountId) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return  emailEvent.name()
                + (agreementId != null && !agreementId.isEmpty() ? "::" + agreementId : "")
                + (discountId != 0 ? "::" + discountId : "")
                + "::" + timeStamp;
    }

    public static String createTrackingKeyForExpirationNotification(DiscountEntity discount,
                                                                    BucketCodeExpiringThresholdEnum threshold) {
        return threshold.name() + "::" + discount.getId() + "::" + Year.now().getValue() + "::" +
               Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }

    public static String createTrackingKeyForWeeklySummaryNotification(ProfileEntity profile) {
        return "WEEKLY-SUMMARY" + "::" + profile.getId() + "::" + Year.now().getValue() + "::" +
               Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }


    private EmailParams createEmailParams(List<String> mailTo,
                                          List<String> mailBcc,
                                          String subject,
                                          String body,
                                          String failureMessage) {
        return createEmailParams(mailTo,
                                 Optional.empty(),
                                 Optional.of(mailBcc),
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
                                 Optional.empty(),
                                 Optional.of(replyToOpt),
                                 subject,
                                 body,
                                 failureMessage,
                                 Optional.empty());
    }

    private EmailParams createEmailParams(String mailTo,
                                          Optional<List<String>> ccList,
                                          Optional<List<String>> bccList,
                                          Optional<String> replyToOpt,
                                          String subject,
                                          String body,
                                          String failureMessage,
                                          Optional<List<Attachment>> attachments) {
        return createEmailParams(Collections.singletonList(mailTo),
                                 ccList,
                                 bccList,
                                 replyToOpt,
                                 subject,
                                 body,
                                 failureMessage,
                                 attachments);
    }

    private EmailParams createEmailParams(List<String> mailTo,
                                          Optional<List<String>> ccList,
                                          Optional<List<String>> bccList,
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
                          .mailBCCList(bccList)
                          .replyToOpt(replyToOpt)
                          .subject(subject)
                          .body(body)
                          .failureMessage(failureMessage)
                          .attachments(attachments)
                          .build();
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
        secondaryReferents.add(configProperties.getCgnDepartmentEmail());

        return secondaryReferents;
    }

    private static class InvalidValueException
            extends RuntimeException {
        public InvalidValueException(String message) {
            super(message);
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
}