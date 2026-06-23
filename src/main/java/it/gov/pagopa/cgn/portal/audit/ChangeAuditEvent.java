package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import lombok.Value;

import java.util.Map;

@Value
public class ChangeAuditEvent {

    String subjectId;
    String partnerFullName;
    String actorRef;
    ChangeAuditSubjectTypeEnum subjectType;
    ChangeAuditOperationTypeEnum operationType;
    Map<String, Object> value;
}