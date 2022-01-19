package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import it.gov.pagopa.cgn.portal.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class EmailNotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    @Autowired
    public EmailNotificationService(JavaMailSender javaMailSender, NotificationRepository notificationRepository) {
        this.javaMailSender = javaMailSender;
        this.notificationRepository = notificationRepository;
    }

    public CompletableFuture<Void> sendAsyncMessage(EmailParams emailParams) {
        return sendAsyncMessage(emailParams, null);
    }

    public CompletableFuture<Void> sendAsyncMessage(EmailParams emailParams, String trackingKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendSyncMessage(emailParams, trackingKey);
            } catch (MessagingException e) {
                log.error(emailParams.getFailureMessage(), e);
            }
            return null;
        });
    }

    public void sendSyncMessage(EmailParams emailParams) throws MessagingException {
        sendSyncMessage(emailParams, null);
    }

    public void sendSyncMessage(EmailParams emailParams, String trackingKey) throws MessagingException {

        if (notificationAlreadySent(trackingKey))
            return;

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(emailParams.getMailFrom());
            helper.setTo(emailParams.getMailToList().toArray(new String[0]));

            if (emailParams.getReplyToOpt().isPresent()) {
                helper.setReplyTo(emailParams.getReplyToOpt().orElseThrow());
            }
            helper.setSubject(emailParams.getSubject());
            helper.setText(emailParams.getBody(), true);
            helper.addInline(emailParams.getLogoName(), emailParams.getLogo());

            log.info("Sending email '{}'", log.isDebugEnabled() ? emailParams.toString() : emailParams.toLightString());
            javaMailSender.send(mimeMessage);
            trackNotification(trackingKey);
        } catch (MessagingException e) {
            trackNotification(trackingKey, e.getMessage());
            throw e;
        }
    }

    private NotificationEntity findNotification(String trackingKey) {
        return notificationRepository.findByKey(trackingKey);
    }

    private void trackNotification(String trackingKey) {
        trackNotification(trackingKey, null);
    }

    private void trackNotification(String trackingKey, String errorMessage) {
        if (trackingKey != null) {
            var notification = findNotification(trackingKey);
            if (notification == null) {
                // create a new Notification
                notification = new NotificationEntity(trackingKey);
            }
            notification.setErrorMessage(errorMessage);
            notificationRepository.save(notification);
        }
    }

    private boolean notificationAlreadySent(String trackingKey) {
        if (trackingKey != null) {
            var notification = findNotification(trackingKey);
            return notification != null && notification.getErrorMessage() == null;
        }
        return false;
    }


}
