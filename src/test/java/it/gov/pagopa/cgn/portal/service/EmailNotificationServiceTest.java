package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import it.gov.pagopa.cgn.portal.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class EmailNotificationServiceTest
        extends IntegrationAbstractTest {

    private final NotificationRepository notificationRepositoryMock;

    private final JavaMailSender javaMailSenderMock;

    private final EmailNotificationService emailNotificationService;

    private final EmailParams emailParams;

    private final MimeMessage expectedMimeMessage;

    ArgumentCaptor<NotificationEntity> argument;

    public EmailNotificationServiceTest() {

        expectedMimeMessage = new MimeMessage((Session) null);

        javaMailSenderMock = Mockito.mock(JavaMailSender.class);
        Mockito.when(javaMailSenderMock.createMimeMessage()).thenReturn(expectedMimeMessage);

        notificationRepositoryMock = Mockito.mock(NotificationRepository.class);

        emailNotificationService = new EmailNotificationService(javaMailSenderMock, notificationRepositoryMock);

        String logoName = "logotest.png";
        Resource logo = Mockito.mock(Resource.class);
        Mockito.when(logo.getFilename()).thenReturn(logoName);

        emailParams = EmailParams.builder()
                                 .mailFrom("test@test.test")
                                 .logoName(logoName)
                                 .logo(logo)
                                 .mailToList(List.of(new String[]{"test@test.test"}))
                                 .mailCCList(Optional.of(List.of(new String[]{"test@test.test"})))
                                 .replyToOpt(Optional.of("test@test.test"))
                                 .subject("test")
                                 .body("test")
                                 .failureMessage("test")
                                 .attachments(Optional.of(new ArrayList<EmailParams.Attachment>()))
                                 .build();

        argument = ArgumentCaptor.forClass(NotificationEntity.class);
    }

    @Test
    void EmailNotificationService_sendSyncMessage_NoNotificationTracking_Ok()
            throws MessagingException {
        emailNotificationService.sendSyncMessage(emailParams);
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(0)).save(argument.capture());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_NoNotificationTracking_Ko()
            throws MessagingException {
        String anErrorMessage = "An error";
        Mockito.doThrow(new RuntimeException(anErrorMessage)).when(javaMailSenderMock).send(expectedMimeMessage);
        Assertions.assertThrows(RuntimeException.class, () -> {
            emailNotificationService.sendSyncMessage(emailParams);
        });
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(0)).save(argument.capture());
    }

    @Test
    void EmailNotificationService_sendAsyncMessage_TrackNotification_Ok() {
        String notificationTrackingKey = "a-tracking-key-async-1";
        emailNotificationService.sendAsyncMessage(emailParams, notificationTrackingKey);
        Mockito.verify(javaMailSenderMock, Mockito.timeout(5000).times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.timeout(5000).times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
    }

    @Test
    void EmailNotificationService_sendAsyncMessage_TrackNotification_Ko() {
        String notificationTrackingKey = "a-tracking-key-async-2";
        String anErrorMessage = "An error";
        Mockito.doThrow(new RuntimeException(anErrorMessage)).when(javaMailSenderMock).send(expectedMimeMessage);
        emailNotificationService.sendAsyncMessage(emailParams, notificationTrackingKey);
        Mockito.verify(javaMailSenderMock, Mockito.timeout(5000).times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.timeout(5000).times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertEquals(anErrorMessage, argument.getValue().getErrorMessage());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_TrackNotification_Ok()
            throws MessagingException {
        String notificationTrackingKey = "a-tracking-key-1";
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey);
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_TrackNotification_Ko() {
        String notificationTrackingKey = "a-tracking-key-2";
        String anErrorMessage = "An error";
        Mockito.doThrow(new RuntimeException(anErrorMessage)).when(javaMailSenderMock).send(expectedMimeMessage);
        Assertions.assertThrows(RuntimeException.class, () -> {
            emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey);
        });
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertEquals(anErrorMessage, argument.getValue().getErrorMessage());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_NoDoubleNotification()
            throws MessagingException {
        String notificationTrackingKey = "a-tracking-key-3";
        Mockito.doReturn(new NotificationEntity(notificationTrackingKey))
               .when(notificationRepositoryMock)
               .findByKey(notificationTrackingKey);
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey);
        Mockito.verify(javaMailSenderMock, Mockito.times(0)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(0)).save(argument.capture());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_RetryNotification()
            throws MessagingException {
        String notificationTrackingKey = "a-tracking-key-4";
        OffsetDateTime errorAt = OffsetDateTime.now().minusMinutes(10);
        String errorMessage = "an error message";
        var notificationToRetry = new NotificationEntity(notificationTrackingKey);
        notificationToRetry.setSentAt(errorAt);
        notificationToRetry.setErrorMessage(errorMessage);
        Mockito.doReturn(notificationToRetry).when(notificationRepositoryMock).findByKey(notificationTrackingKey);
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey);
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(1)).save(argument.capture());
        // the new notification should have the same key, no error message and a sent_at greater than the one in error
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertNull(argument.getValue().getErrorMessage());
        Assertions.assertNotEquals(errorAt, argument.getValue().getSentAt());
        Assertions.assertTrue(errorAt.isBefore(argument.getValue().getSentAt()));
    }
}
