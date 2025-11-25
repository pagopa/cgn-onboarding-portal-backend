package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.email.EmailParams;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles({"dev"})
public class SendDiscountsToEycaJobTest extends IntegrationAbstractTest {

    @MockBean
    private EmailNotificationService emailNotificationService;

    @Autowired
    private EmailNotificationFacade emailNotificationFacade;

    @BeforeEach
    void before() {
        setAdminAuth();
    }

    @Test
    void notifyAdminForJobEyca_shouldRenderExpectedValues() {
        int entitiesToCreateOnEyca = 12;
        int entitiesToUpdateOnEyca = 34;
        int entitiesToDeleteOnEyca = 10;

        doReturn(CompletableFuture.completedFuture(null))
                .when(emailNotificationService)
                .sendAsyncMessage(any(), anyString(), anyString());

        emailNotificationFacade.notifyAdminForJobEyca(
                Collections.emptyList(),
                entitiesToCreateOnEyca,
                entitiesToUpdateOnEyca,
                entitiesToDeleteOnEyca
        );

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

            ArgumentCaptor<EmailParams> captor = ArgumentCaptor.forClass(EmailParams.class);

            verify(emailNotificationService, atLeastOnce())
                    .sendAsyncMessage(captor.capture(), anyString(), anyString());

            String body = captor.getValue().getBody();
            assertNotNull(body);

            assertTrue(body.contains("Opportunità da creare"));
            assertTrue(body.contains("Opportunità da aggiornare"));
            assertTrue(body.contains("Opportunità da cancellare"));

            assertTrue(body.contains("<span>" + entitiesToCreateOnEyca + "</span>"));
            assertTrue(body.contains("<span>" + entitiesToUpdateOnEyca + "</span>"));
            assertTrue(body.contains("<span>" + entitiesToDeleteOnEyca + "</span>"));
        });
    }
}
