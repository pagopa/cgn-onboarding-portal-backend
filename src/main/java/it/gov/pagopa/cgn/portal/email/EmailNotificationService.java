package it.gov.pagopa.cgn.portal.email;

import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import it.gov.pagopa.cgn.portal.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class EmailNotificationService {

    public static final int MAX_EXCEPTION_DEEP = 10;
    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    @Autowired
    public EmailNotificationService(JavaMailSender javaMailSender, NotificationRepository notificationRepository) {
        this.javaMailSender = javaMailSender;
        this.notificationRepository = notificationRepository;
    }

    public CompletableFuture<Void> sendAsyncMessage(EmailParams emailParams) {
        return sendAsyncMessage(emailParams, null,null);
    }

    public CompletableFuture<Void> sendAsyncMessage(EmailParams emailParams, String trackingKey, String info) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendSyncMessage(emailParams, trackingKey, info);
            } catch (MessagingException e) {
                log.error(emailParams.getFailureMessage(), e);
            }
            return null;
        });
    }

    public void sendSyncMessage(EmailParams emailParams)
            throws MessagingException {
        sendSyncMessage(emailParams, null, null);
    }

    public void sendSyncMessage(EmailParams emailParams, String trackingKey, String info)
            throws MessagingException {

        if (notificationAlreadySent(trackingKey)) return;

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(emailParams.getMailFrom());
            helper.setTo(emailParams.getMailToList().toArray(new String[0]));

            if (emailParams.getMailCCList().isPresent()) {
                helper.setCc(emailParams.getMailCCList().orElseThrow().toArray(new String[0]));
            }

            if (emailParams.getMailBCCList().isPresent()) {
                helper.setBcc(emailParams.getMailBCCList().orElseThrow().toArray(new String[0]));
            }

            if (emailParams.getReplyToOpt().isPresent()) {
                helper.setReplyTo(emailParams.getReplyToOpt().orElseThrow());
            }

            helper.setSubject(emailParams.getSubject());
            helper.setText(emailParams.getBody(), true);
            helper.addInline(emailParams.getLogoName(), emailParams.getLogo());

            if (emailParams.getAttachments().isPresent()) {
                emailParams.getAttachments().get().forEach(attachment -> {
                    try {
                        helper.addAttachment(attachment.getAttachmentFilename(), attachment.getResource());
                    } catch (MessagingException e) {
                        throw new InvalidFileNameException(attachment.getAttachmentFilename(),e.getMessage());
                    }
                });
            }

            log.info("Sending email '{}'", log.isDebugEnabled() ? emailParams.toString():emailParams.toLightString());
            javaMailSender.send(mimeMessage);
            trackNotification(trackingKey,null,info);
        } catch (Exception e) {
            trackNotification(trackingKey, getRootMessage(e), getInfoFromSendFailedException(e));
            throw e;
        }
    }

    private SendFailedException findSendFailedException(Throwable e) {
        return Stream.iterate(e, Objects::nonNull, Throwable::getCause)
                     .limit(MAX_EXCEPTION_DEEP)
                     .filter(SendFailedException.class::isInstance)
                     .map(SendFailedException.class::cast)
                     .findFirst()
                     .orElse(null);
    }

    private String getRootMessage(Throwable e) {
        Throwable trRoot = Stream.iterate(e, Objects::nonNull, Throwable::getCause)
                                 .limit(MAX_EXCEPTION_DEEP)
                                 .reduce((a, b) -> b)
                                 .orElse(null);

        return trRoot!=null ? trRoot.toString(): e.toString();
    }

    private String getInfoFromSendFailedException(Throwable t) {
        SendFailedException sfe = findSendFailedException(t);

        if(sfe == null) return null;

        String invalid = addressesToString("invalid", sfe.getInvalidAddresses());
        String unsent  = addressesToString("unsent",  sfe.getValidUnsentAddresses());

        return Stream.of(invalid, unsent)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.joining("; "));
    }

    private String addressesToString(String label, Address[] addresses) {
        return (addresses != null && addresses.length > 0)
               ? label + ": " + Arrays.stream(addresses)
                                      .map(Address::toString)
                                      .collect(Collectors.joining(", "))
               : "";
    }
    private NotificationEntity findNotification(String trackingKey) {
        return notificationRepository.findByKey(trackingKey);
    }

    /**
     * Saves a new Notification, or updates the existing one if already existent,
     * for a given key.
     *
     * @param trackingKey  a key that uniquely identify this notification
     * @param errorMessage a message that indicates any error occurred
     */
    public void trackNotification(String trackingKey, String errorMessage, String info) {
        if (trackingKey!=null) {
            // if a key has been given we check if a notification exists
            // this is useful to update a notification that had an error
            var notification = findNotification(trackingKey);
            if (notification==null) {
                // if no notification is found create a new Notification
                notification = new NotificationEntity(trackingKey);
            }
            notification.setSentAt(OffsetDateTime.now());
            notification.setErrorMessage(errorMessage);
            notification.setInfo(info);
            notificationRepository.save(notification);
        }
    }

    /**
     * Returns a boolean indicating whether a given notification exists
     * for a given key and was sent without errors.
     *
     * @param trackingKey the tracking key of the notification if any
     * @return boolean
     */
    private boolean notificationAlreadySent(String trackingKey) {
        if (trackingKey!=null) {
            // if a key has been given we check if a notification exist
            var notification = findNotification(trackingKey);
            return notification!=null && notification.getErrorMessage()==null;
        }
        return false;
    }


}
