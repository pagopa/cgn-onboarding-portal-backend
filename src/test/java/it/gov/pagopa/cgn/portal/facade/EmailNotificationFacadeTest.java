package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationEventEnum;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
class EmailNotificationFacadeTest {
    @Mock
    private TemplateEngine htmlTemplateEngine;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private ParamFacade paramFacade;

    @InjectMocks
    private EmailNotificationFacade emailNotificationFacade;

    private static final Pattern TS_PATTERN = Pattern.compile("\\d{17}"); // yyyyMMddHHmmssSSS

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(paramFacade.getEycaAdminMailTo()).thenReturn(new String[]{"eycaMailTo@contoso.com"});
        when(paramFacade.getEycaJobMailTo()).thenReturn(new String[]{"eycaJobMailTo@contoso.com"});

        emailNotificationFacade.init();
    }


    @Test
    void createEmailParams_shouldUseBccWhenPresent() {
        String body = "fake body";

        emailNotificationFacade.notifyEycaAdmin(body);

        ArgumentCaptor<EmailParams> emailParamsCaptor = ArgumentCaptor.forClass(EmailParams.class);
        ArgumentCaptor<String> trackingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailNotificationService).sendAsyncMessage(
                emailParamsCaptor.capture(),
                trackingKeyCaptor.capture(),
                anyString()
        );

        EmailParams params = emailParamsCaptor.getValue();
        assertNotNull("EmailParams non deve essere null",params);
        assertNotNull("Bcc non deve essere null",params.getMailBCCList());
    }
    @Test
    void notifyMerchantDiscountTestFailed_shouldFillTemplateWithContextValues() {

        String discountName = "Sconto Speciale";
        String reasonMessage = "Il test è fallito per errore tecnico.";

        String simulatedHtmlBody = "<html><body><p>il team di CGN ha effettuato i test tecnici sull’agevolazione Sconto Speciale, secondo la modalità di" +
                                   "riconoscimento che avete scelto.</p><p>Purtroppo l’esito è negativo con la seguente motivazione: " +
                                   "Il test è fallito per errore tecnico.</p></body></html>";

        when(htmlTemplateEngine.process(anyString(), any(Context.class))).thenReturn(simulatedHtmlBody);
        ReferentEntity re = new ReferentEntity();
        re.setEmailAddress("emailaddress@contoso.com");
        DiscountEntity de = new DiscountEntity();
        de.setId(123L);
        de.setName(discountName);
        AgreementEntity ae = new AgreementEntity();
        ae.setId("xxx-xxx");
        ProfileEntity pe = new ProfileEntity();
        pe.setReferent(re);
        pe.setAgreement(ae);

        emailNotificationFacade.notifyMerchantDiscountTestFailed(pe, de, reasonMessage);

        // Capture the actual template and context passed
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(htmlTemplateEngine).process(templateCaptor.capture(), contextCaptor.capture());

        String returnedHtmlBody = htmlTemplateEngine.process(templateCaptor.getValue(), contextCaptor.getValue());

        assertNotNull(contextCaptor.getValue().getVariable(EmailNotificationFacade.FAILURE_REASON));

        // Assert
        assertTrue("Not expected discountName", returnedHtmlBody.contains(discountName));
        assertTrue("Not expected reason message", returnedHtmlBody.contains(reasonMessage));
    }

    @Test
    void createEmailParams_shouldBePresentRecipientsSummary() {
        String body = "fake body";

        emailNotificationFacade.notifyEycaAdmin(body);

        ArgumentCaptor<String> recipientSummaryCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailNotificationService).sendAsyncMessage(
                any(),
                anyString(),
                recipientSummaryCaptor.capture()
        );

        String recipientSummary = recipientSummaryCaptor.getValue();

        assertNotNull("recipientSummary non deve essere null",recipientSummary);
        assertEquals("recipientSummary deve contenere To e Bcc", "To:eycaMailTo@contoso.com Bcc:eycaJobMailTo@contoso.com", recipientSummaryCaptor.getValue());

    }

    @Test
    void createEmailParams_RecipientsSummary_shouldHaveOnlyBccWhenSuspendReferentsMailSendingIsTrueForEyca() {
        String body = "fake body";

        when(paramFacade.getSuspendReferentsMailSending()).thenReturn("true");

        emailNotificationFacade.notifyEycaAdmin(body);

        ArgumentCaptor<String> recipientSummaryCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailNotificationService).sendAsyncMessage(
                any(),
                anyString(),
                recipientSummaryCaptor.capture()
        );

        String recipientSummary = recipientSummaryCaptor.getValue();

        assertNotNull("recipientSummary non deve essere null",recipientSummary);
        assertEquals("recipientSummary deve contenere solo bcc", "Bcc:eycaJobMailTo@contoso.com", recipientSummaryCaptor.getValue());

    }

    @Test
    void createEmailParams_RecipientsSummary_shouldHaveOnlyBccWhenSuspendReferentsMailSendingIsTrue() {

        when(paramFacade.getSuspendReferentsMailSending()).thenReturn("true");

        ReferentEntity re = new ReferentEntity();
        re.setEmailAddress("emailaddress@contoso.com");
        DiscountEntity de = new DiscountEntity();
        de.setId(123L);
        de.setName("discountName");
        AgreementEntity ae = new AgreementEntity();
        ae.setId("xxx-xxx");
        ProfileEntity pe = new ProfileEntity();
        pe.setReferent(re);
        pe.setAgreement(ae);
        de.setAgreement(ae);
        ae.setProfile(pe);

        emailNotificationFacade.notifyMerchantDiscountBucketCodesExpired(de);

        ArgumentCaptor<String> recipientSummaryCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailNotificationService).sendAsyncMessage(
                any(),
                anyString(),
                recipientSummaryCaptor.capture()
        );

        String recipientSummary = recipientSummaryCaptor.getValue();

        assertNotNull("recipientSummary non deve essere null",recipientSummary);
        assertEquals("recipientSummary deve contenere solo bcc", "Bcc:eycaJobMailTo@contoso.com", recipientSummaryCaptor.getValue());

    }

    @Test
    void createKey_allParams() {
        String key = EmailNotificationFacade.createTrackingKeyForEmailEventNotification(
                EmailNotificationEventEnum.NEW_AGREEMENT_REQUEST, "AG123", 42L
        );

        // NEW_AGREEMENT_REQUEST::AG123::42::<timestamp>
        String[] parts = key.split("::");
        assertEquals(4, parts.length);
        assertEquals("NEW_AGREEMENT_REQUEST", parts[0]);
        assertEquals("AG123", parts[1]);
        assertEquals("42", parts[2]);
        assertTrue("Timestamp atteso con 17 cifre", TS_PATTERN.matcher(parts[3]).matches());
    }

    @Test
    void createKey_emptyAgreementId() {
        String key = EmailNotificationFacade.createTrackingKeyForEmailEventNotification(
                EmailNotificationEventEnum.HELP_REQUEST, "", 7L
        );

        // HELP_REQUEST::7::<timestamp>
        String[] parts = key.split("::");
        assertEquals(3, parts.length);
        assertEquals("HELP_REQUEST", parts[0]);
        assertEquals("7", parts[1]);
        assertTrue(TS_PATTERN.matcher(parts[2]).matches());
    }

    @Test
    void createKey_nullAgreementId() {
        String key = EmailNotificationFacade.createTrackingKeyForEmailEventNotification(
                EmailNotificationEventEnum.DISCOUNT_TEST_PASSED, null, 999L
        );

        // DISCOUNT_TEST_PASSED::999::<timestamp>
        String[] parts = key.split("::");
        assertEquals(3, parts.length);
        assertEquals("DISCOUNT_TEST_PASSED", parts[0]);
        assertEquals("999", parts[1]);
        assertTrue(TS_PATTERN.matcher(parts[2]).matches());
    }

    @Test
    void createKey_discountZero() {
        String key = EmailNotificationFacade.createTrackingKeyForEmailEventNotification(
                EmailNotificationEventEnum.DISCOUNT_TEST_FAILED, "AG777", 0L
        );

        // DISCOUNT_TEST_FAILED::AG777::<timestamp>
        String[] parts = key.split("::");
        assertEquals(3, parts.length);
        assertEquals("DISCOUNT_TEST_FAILED", parts[0]);
        assertEquals("AG777", parts[1]);
        assertTrue(TS_PATTERN.matcher(parts[2]).matches());
    }

    @Test
    @DisplayName("Solo event: timestamp presente")
    void createKey_onlyEvent() {
        String key = EmailNotificationFacade.createTrackingKeyForEmailEventNotification(
                EmailNotificationEventEnum.ADMIN_EYCA, null, 0L
        );

        // ADMIN_EYCA::<timestamp>
        String[] parts = key.split("::");
        assertEquals(2, parts.length);
        assertEquals("ADMIN_EYCA", parts[0]);
        assertTrue(TS_PATTERN.matcher(parts[1]).matches());
    }
}