package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.util.Collections;
import java.util.Optional;

@Component
public class DebugUtil {

    private static EmailNotificationService emailNotificationService;
    private static ConfigProperties configProperties;

    @Autowired
    public DebugUtil(EmailNotificationService service, ConfigProperties properties) {
        DebugUtil.emailNotificationService = service;
        DebugUtil.configProperties = properties;
    }


    public static void sendEmail(String to, String subject, String body) throws MessagingException {

        EmailParams params =  EmailParams.builder()
                .mailFrom(configProperties.getCgnNotificationSender())
                .logoName("")
                .logo(configProperties.getCgnLogo())
                .mailToList(Collections.singletonList(to))
                .mailCCList(Optional.empty())
                .replyToOpt(Optional.empty())
                .subject(subject)
                .body(body)
                .failureMessage("")
                .build();

        emailNotificationService.sendSyncMessage(params);

    }

}
