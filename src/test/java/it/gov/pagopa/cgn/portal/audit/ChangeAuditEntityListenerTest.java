package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.config.SpringContextHolder;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;

class ChangeAuditEntityListenerTest {

    @Test
    void Publish_ShouldIgnoreUnauditedEntitiesWithoutPublisherBeanLookup() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        ChangeAuditPayloadFactory payloadFactory = Mockito.mock(ChangeAuditPayloadFactory.class);
        Mockito.when(applicationContext.getBean(ChangeAuditPayloadFactory.class)).thenReturn(payloadFactory);
        Mockito.when(payloadFactory.build(Mockito.any(), Mockito.eq(ChangeAuditOperationTypeEnum.INSERT)))
               .thenReturn(Optional.empty());
        new SpringContextHolder().setApplicationContext(applicationContext);

        ChangeAuditEntityListener listener = new ChangeAuditEntityListener();

        Assertions.assertDoesNotThrow(() -> listener.onPostPersist(new AgreementUserEntity()));
        Mockito.verify(applicationContext, Mockito.never()).publishEvent(Mockito.any());
    }

    @Test
    void Publish_ShouldDispatchBuiltAuditEvent() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        ChangeAuditPayloadFactory payloadFactory = Mockito.mock(ChangeAuditPayloadFactory.class);
        Mockito.when(applicationContext.getBean(ChangeAuditPayloadFactory.class)).thenReturn(payloadFactory);
        ChangeAuditEvent event = new ChangeAuditEvent(
                "agreement-id",
                "partner",
                "actor",
                ChangeAuditSubjectTypeEnum.AGREEMENT,
                ChangeAuditOperationTypeEnum.INSERT,
                Map.of("agreement_k", "agreement-id")
        );
        Mockito.when(payloadFactory.build(Mockito.any(), Mockito.eq(ChangeAuditOperationTypeEnum.INSERT)))
               .thenReturn(Optional.of(event));
        new SpringContextHolder().setApplicationContext(applicationContext);

        ChangeAuditEntityListener listener = new ChangeAuditEntityListener();

        listener.onPostPersist(new AgreementUserEntity());

        Mockito.verify(applicationContext).publishEvent(event);
    }
}