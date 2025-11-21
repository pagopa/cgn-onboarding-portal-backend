package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.email.TemplateEmail;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EmailTemplateTest
        extends IntegrationAbstractTest {

    @Autowired
    protected TemplateEngine htmlTemplateEngine;

    @Test
    void templateFileIsOnClasspath() {
        verifyIsOnPath(TemplateEmail.CLEAN_DISCOUNT_BUCKET_CODES);
        verifyIsOnPath(TemplateEmail.SEND_DISCOUNTS_TO_EYCA);
    }

    @Test
    void bodyShouldContainExpectedValuesForCleanDiscountBucketCodes() {
        long deletedCodes = 1234L;
        String retentionPeriod = "P180D";
        Instant cutoff = Instant.parse("2025-01-01T00:00:00Z");
        Duration execTime = Duration.ofSeconds(12);

        Context ctx = new Context();
        ctx.setVariable("deletedCodes", deletedCodes);
        ctx.setVariable("retentionPeriod", retentionPeriod);
        ctx.setVariable("cutoff", cutoff);
        ctx.setVariable("execTime", execTime);

        String body = htmlTemplateEngine.process(TemplateEmail.CLEAN_DISCOUNT_BUCKET_CODES.getTemplateName(), ctx);

        assertNotNull(body);
        assertTrue(body.contains("Clean Discount Bucket Codes job è terminato con successo."));

        assertTrue(body.contains(String.valueOf(deletedCodes)),
                   "Body should contain deletedCodes");
        assertTrue(body.contains(retentionPeriod),
                   "Body should contain retentionPeriod");
        assertTrue(body.contains(cutoff.toString()),
                   "Body should contain cutoff Instant");
        assertTrue(body.contains(execTime.toString()),
                   "Body should contain execTime Duration");
    }


    @Test
    void bodyShouldContainExpectedValuesForSendDiscountsToEyca() {
        int entitiesToCreateOnEyca = 12;
        int entitiesToUpdateOnEyca = 34;
        int entitiesToDeleteOnEyca = 10;

        Context ctx = new Context();
        ctx.setVariable("entitiesToCreateOnEyca", entitiesToCreateOnEyca);
        ctx.setVariable("entitiesToUpdateOnEyca", entitiesToUpdateOnEyca);
        ctx.setVariable("entitiesToDeleteOnEyca", entitiesToDeleteOnEyca);

        String body = htmlTemplateEngine.process(TemplateEmail.SEND_DISCOUNTS_TO_EYCA.getTemplateName(), ctx);

        assertNotNull(body);
        assertTrue(body.contains("Send Discounts To Eyca Job è terminato con successo."));

        assertTrue(body.contains(String.valueOf(entitiesToCreateOnEyca)),
                   "Body should contain entitiesToCreateOnEyca");
        assertTrue(body.contains(String.valueOf(entitiesToUpdateOnEyca)),
                   "Body should contain entitiesToUpdateOnEyca");
        assertTrue(body.contains(String.valueOf(entitiesToDeleteOnEyca)),
                   "Body should contain entitiesToDeleteOnEyca");
    }

    private void verifyIsOnPath(@NotNull TemplateEmail te) {
        var is = getClass().getClassLoader().getResourceAsStream("templates/"+te.getTemplateName());
        assertNotNull(is, String.format ("%s Template HTML non trovato sul classpath",te));
    }

}