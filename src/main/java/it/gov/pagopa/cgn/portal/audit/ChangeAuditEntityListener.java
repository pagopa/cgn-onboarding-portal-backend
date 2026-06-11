package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.config.SpringContextHolder;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;

@Slf4j
public class ChangeAuditEntityListener {

    @PostPersist
    public void onPostPersist(Object entity) {
        publish(entity, ChangeAuditOperationTypeEnum.INSERT);
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        publish(entity, ChangeAuditOperationTypeEnum.UPDATE);
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        publish(entity, ChangeAuditOperationTypeEnum.DELETE);
    }

    private void publish(Object entity, ChangeAuditOperationTypeEnum operationType) {
        if (!SpringContextHolder.isReady()) {
            return;
        }

        try {
            ChangeAuditPayloadFactory payloadFactory = SpringContextHolder.getBean(ChangeAuditPayloadFactory.class);
            payloadFactory.build(entity, operationType)
                          .ifPresent(SpringContextHolder::publishEvent);
        } catch (Exception ex) {
            log.error("Unable to publish change audit event for entity type {}", entity.getClass().getName(), ex);
        }
    }
}