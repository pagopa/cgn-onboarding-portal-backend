package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class EmailNotificationFacadeTest {
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

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void createEmailParams_shouldUseBccWhenPresent() {
        String body = "fake body";

        when(paramFacade.getEycaAdminMailTo()).thenReturn(new String[]{"eycaMailTo@contoso.com"});
        when(paramFacade.getEycaJobMailTo()).thenReturn(new String[]{"eycaJobMailTo@contoso.com"});

        emailNotificationFacade.notifyEycaAdmin(body);

        ArgumentCaptor<EmailParams> emailParamsCaptor = ArgumentCaptor.forClass(EmailParams.class);

        verify(emailNotificationService).sendAsyncMessage(emailParamsCaptor.capture());

        assertNotNull(emailParamsCaptor.getValue().getMailBCCList());
    }
    @Test
    public void notifyMerchantDiscountTestFailed_shouldFillTemplateWithContextValues() {

        String discountName = "Sconto Speciale";
        String reasonMessage = "Il test è fallito per errore tecnico.";

        String simulatedHtmlBody = "<html><body><p>il team di CGN ha effettuato i test tecnici sull’agevolazione Sconto Speciale, secondo la modalità di" +
                                   "riconoscimento che avete scelto.</p><p>Purtroppo l’esito è negativo con la seguente motivazione: " +
                                   "Il test è fallito per errore tecnico.</p></body></html>";

        when(htmlTemplateEngine.process(anyString(), any(Context.class))).thenReturn(simulatedHtmlBody);
        ProfileEntity pe = new ProfileEntity();
        ReferentEntity re = new ReferentEntity();
        re.setEmailAddress("emailaddress@contoso.com");
        pe.setReferent(re);
        emailNotificationFacade.notifyMerchantDiscountTestFailed(pe, discountName, reasonMessage);

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

}