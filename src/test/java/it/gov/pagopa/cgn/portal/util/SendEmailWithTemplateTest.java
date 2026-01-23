package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.email.TemplateEmail;
import it.gov.pagopa.cgn.portal.service.ExportService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.util.List;

@Disabled("Test disabilitato per invio email reale")
class SendEmailWithTemplateTest {

    @Test
    void testSendEycaManualChangesToDept() throws MessagingException {
        TemplateEmailSender tEmailSender = new TemplateEmailSender();

        var rowForCreate = ExportService.EycaManualRowView.of(
            "103296", "cB40202601220020218773225", "eCampus Università Telematica", "ECAMPUSSP65", "No Limit",
            "2025-07-15", "2026-02-28"
        );

        var rowForUpdate = ExportService.EycaManualRowView.of(
            "113294", "cB302025016208720218773222", "Azienda Test", "https://example.com/provalinkestremamentelungo",
            "No Limit", "2024-02-01", "2025-02-28"
        );

        var rowForDelete = ExportService.EycaManualRowView.of(
            "163247", "cB302025016208720218773222", "Vecchia Azienda", "OLDCODE", "No Limit",
            "2023-01-01", "2023-12-31"
        );

        Context context = new Context();
        context.setVariable("createdOnEyca", List.of(rowForCreate));
        context.setVariable("toUpdateOnEyca", List.of(rowForUpdate));
        context.setVariable("toDeleteOnEyca", List.of(rowForDelete));

        tEmailSender.sendEmail("Test invio", TemplateEmail.SEND_EYCA_MANUAL_CHANGES_TO_DEPT,context);

        
    }
}
