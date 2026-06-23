package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.service.ChangeAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChangeAuditTransactionalListener {

    private final ChangeAuditService changeAuditService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChangeAuditEvent event) {
        try {
            changeAuditService.save(event);
        } catch (Exception ex) {
            log.error("Unable to persist change audit for subjectType={} subjectId={}",
                      event.getSubjectType(),
                      event.getSubjectId(),
                      ex);
        }
    }
}