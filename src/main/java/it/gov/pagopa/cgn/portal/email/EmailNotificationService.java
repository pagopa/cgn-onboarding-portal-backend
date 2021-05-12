package it.gov.pagopa.cgn.portal.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
public class EmailNotificationService {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailNotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMessage(EmailParams emailParams) throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

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
    }


}
