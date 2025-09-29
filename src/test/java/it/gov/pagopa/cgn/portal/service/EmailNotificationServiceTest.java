package it.gov.pagopa.cgn.portal.service;

import com.sun.mail.smtp.SMTPAddressFailedException;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import it.gov.pagopa.cgn.portal.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.SocketTimeoutException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Spy
    @InjectMocks
    EmailNotificationService emailNotificationServiceSpy;

    @Captor
    ArgumentCaptor<String> messageCaptor;

    @Captor
    ArgumentCaptor<String> infoCaptor;


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
                                 .mailBCCList(Optional.of(List.of(new String[]{"test@test.test"})))
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
    void EmailNotificationService_sendSyncMessage_NoNotificationTracking_Ko() {
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
        String info = "info";
        emailNotificationService.sendAsyncMessage(emailParams, notificationTrackingKey,info);
        Mockito.verify(javaMailSenderMock, Mockito.timeout(5000).times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.timeout(5000).times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertEquals(info, argument.getValue().getInfo());
    }

    @Test
    void EmailNotificationService_sendAsyncMessage_TrackNotification_Ko() {
        String notificationTrackingKey = "a-tracking-key-async-2";
        String anErrorMessage = "An error";
        String info = "info";
        Mockito.doThrow(new RuntimeException(anErrorMessage)).when(javaMailSenderMock).send(expectedMimeMessage);
        emailNotificationService.sendAsyncMessage(emailParams, notificationTrackingKey,info);
        Mockito.verify(javaMailSenderMock, Mockito.timeout(5000).times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.timeout(5000).times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertEquals("java.lang.RuntimeException: An error", argument.getValue().getErrorMessage());
        Assertions.assertNull(argument.getValue().getInfo());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_TrackNotification_Ok()
            throws MessagingException {
        String notificationTrackingKey = "a-tracking-key-1";
        String info = "info";
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey, info);
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertEquals(info, argument.getValue().getInfo());

    }

    @Test
    void EmailNotificationService_sendSyncMessage_TrackNotification_Ko() {
        String notificationTrackingKey = "a-tracking-key-2";
        String anErrorMessage = "An error";
        String info = "info";
        Mockito.doThrow(new RuntimeException(anErrorMessage)).when(javaMailSenderMock).send(expectedMimeMessage);
        Assertions.assertThrows(RuntimeException.class, () -> {
            emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey, info);
        });
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(1)).save(argument.capture());
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertEquals("java.lang.RuntimeException: An error", argument.getValue().getErrorMessage());
        Assertions.assertNull(argument.getValue().getInfo());
    }

    @Test
    void EmailNotificationService_sendSyncMessage_NoDoubleNotification()
            throws MessagingException {
        String notificationTrackingKey = "a-tracking-key-3";
        Mockito.doReturn(new NotificationEntity(notificationTrackingKey))
               .when(notificationRepositoryMock)
               .findByKey(notificationTrackingKey);
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey, null);
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
        emailNotificationService.sendSyncMessage(emailParams, notificationTrackingKey,null);
        Mockito.verify(javaMailSenderMock, Mockito.times(1)).send(expectedMimeMessage);
        Mockito.verify(notificationRepositoryMock, Mockito.times(1)).save(argument.capture());
        // the new notification should have the same key, no error message and a sent_at greater than the one in error
        Assertions.assertEquals(notificationTrackingKey, argument.getValue().getKey());
        Assertions.assertNull(argument.getValue().getErrorMessage());
        Assertions.assertNotEquals(errorAt, argument.getValue().getSentAt());
        Assertions.assertTrue(errorAt.isBefore(argument.getValue().getSentAt()));
    }

    @Test
    void shouldTrackNotification_whenThrowsSendFailedException() throws  Exception {
        SendFailedException sfe = getSendFailedException();
        MessagingException sfMex = new MessagingException("Exception reading response", sfe);

        String trackingKey = "TK-123";
        String info = "unit-test";

        when(notificationRepositoryMock.findByKey(anyString())).thenReturn(null);

        Mockito.doThrow(new MailSendException("send failed", sfMex)).when(javaMailSenderMock).send(expectedMimeMessage);

        Resource logo = Mockito.mock(Resource.class);
        Mockito.when(logo.getFilename()).thenReturn("logoName");

        EmailParams emailParams = getEmailParams(logo);

        assertThrows(
                MailSendException.class,
                () -> emailNotificationServiceSpy.sendSyncMessage(emailParams, trackingKey, info)
        );

        verify(emailNotificationServiceSpy).trackNotification(any(), messageCaptor.capture(), infoCaptor.capture());

        Assertions.assertEquals(
                "com.sun.mail.smtp.SMTPAddressFailedException: 5.7.1 <pippo.franco@rai.it>: Recipient address rejected: Access denied",
                messageCaptor.getValue());

        Assertions.assertEquals("invalid: pippo.franco@rai.it", infoCaptor.getValue());


    }

    @Test
    void shouldTrackNotification_whenThrowsSocketTimeoutException() {

        SocketTimeoutException ste = new SocketTimeoutException("Read timed out");
        MailException mMex = new MailSendException("Exception reading response", ste);

        String trackingKey = "TK-123";
        String info = "unit-test";

        when(notificationRepositoryMock.findByKey(anyString())).thenReturn(null);

        Mockito.doThrow(mMex).when(javaMailSenderMock).send(expectedMimeMessage);

        Resource logo = Mockito.mock(Resource.class);
        Mockito.when(logo.getFilename()).thenReturn("logoName");

        EmailParams emailParams = getEmailParams(logo);

        assertThrows(
                MailSendException.class,
                () -> emailNotificationServiceSpy.sendSyncMessage(emailParams, trackingKey, info)
        );

        verify(emailNotificationServiceSpy).trackNotification(any(), messageCaptor.capture(), any());

        Assertions.assertEquals("java.net.SocketTimeoutException: Read timed out", messageCaptor.getValue());

    }

    private EmailParams getEmailParams(Resource logo) {
        return EmailParams.builder()
                          .replyToOpt(Optional.of("noreply@test.local"))
                          .mailFrom("noreply@test.local")
                          .mailToList(List.of("user@example.com"))
                          .mailCCList(Optional.of(List.of("user@example.com")))
                          .mailBCCList(Optional.of(List.of("user@example.com")))
                          .subject("Subject")
                          .body("Body")
                          .logo(logo)
                          .logoName("logoName")
                          .attachments(Optional.empty())
                          .build();
    }

    private SendFailedException getSendFailedException()
            throws AddressException {
        InternetAddress invalid = new InternetAddress("pippo.franco@rai.it");

        SMTPAddressFailedException smtpEx =
                new SMTPAddressFailedException(
                        invalid,
                        "RCPT TO",           // comando SMTP
                        554,                 // reply code
                        "5.7.1 <" + invalid.toString() + ">: Recipient address rejected: Access denied"
                );

        return new SendFailedException(
                "Invalid Addresses",
                smtpEx,        // nested
                null,                // validSent
                null,                // validUnsent
                new Address[]{invalid}
        );
    }
}
