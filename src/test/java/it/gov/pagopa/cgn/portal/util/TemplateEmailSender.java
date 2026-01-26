package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.email.TemplateEmail;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class TemplateEmailSender {
    
    protected static final String DEFAULT_TEMPLATE_PATH = "src/main/resources/templates/";

    /*
     * configure SMTP credentials and from/to addresses locally before running.
     * This test sends a real email.
     *
     * IMPORTANT:
     * - Do not commit real credentials to the repository.
     */

    protected static TemplateEngine templateEngine;
    protected static final String SMTP_HOST = "smtp.gmail.com";
    protected static final int SMTP_PORT = 587;
    protected static final String SMTP_USERNAME = "";
    protected static final String SMTP_PASSWORD = "";
    protected static final String EMAIL_FROM ="";
    protected static final String EMAIL_TO="";


    public TemplateEmailSender() {
        templateEngine = initializeTemplateEngine();
    }

    private TemplateEngine initializeTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(DEFAULT_TEMPLATE_PATH);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        engine.setTemplateResolver(resolver);
        return engine;
    }

    protected void sendEmail(String subject, TemplateEmail tEmail, Context context ) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.smtp.ssl.checkserveridentity", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_TO));
        message.setSubject(subject);
        message.setText(templateEngine.process(tEmail.getTemplateName(), context), "utf-8", "html");

        Transport.send(message);
    }
}
