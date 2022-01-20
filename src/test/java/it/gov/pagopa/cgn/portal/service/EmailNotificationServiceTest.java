package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import it.gov.pagopa.cgn.portal.repository.NotificationRepository;
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
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("dev")
public class EmailNotificationServiceTest extends IntegrationAbstractTest {

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
                .replyToOpt(Optional.of("test@test.test"))
                .subject("test")
                .body("test")
                .failureMessage("test")
                .build();

        argument = ArgumentCaptor.forClass(NotificationEntity.class);
    }

    @Test
    void EmailNotificationService_sendSyncMessage_TrackNotification_Ok() throws MessagingException {
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
    void EmailNotificationService_sendSyncMessage_NoDoubleNotification() throws MessagingException {
        String notificationTrackingKey = "a-tracking-key-3";
        Mockito.doReturn(new NotificationEntity(notificationTrackingKey)).when(notificationRepositoryMock).findByKey(notificationTrackingKey);
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey);
        Mockito.verify(javaMailSenderMock, Mockito.times(0)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(0)).save(argument.capture());
    }
}
