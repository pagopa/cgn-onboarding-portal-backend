package it.gov.pagopa.cgn.portal.audit;

import it.gov.pagopa.cgn.portal.annotation.Audited;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChangeAuditPayloadFactory {

    private final ChangeAuditActorRefResolver actorRefResolver;
    private final ChangeAuditPartnerNameResolver partnerNameResolver;

    public Optional<ChangeAuditEvent> build(Object entity, ChangeAuditOperationTypeEnum operationType) {
        Class<?> entityClass = Hibernate.getClass(entity);
        if (!entityClass.isAnnotationPresent(Audited.class)) {
            return Optional.empty();
        }

        if (entity instanceof AgreementEntity agreement) {
            return Optional.of(buildAgreementEvent(agreement, operationType));
        }

        return Optional.empty();
    }

    private ChangeAuditEvent buildAgreementEvent(AgreementEntity agreement,
                                                 ChangeAuditOperationTypeEnum operationType) {
        return new ChangeAuditEvent(
                agreement.getId(),
                partnerNameResolver.resolveForAgreement(agreement),
                actorRefResolver.resolve(),
                ChangeAuditSubjectTypeEnum.AGREEMENT,
                operationType,
                createAgreementSnapshot(agreement)
        );
    }

    private Map<String, Object> createAgreementSnapshot(AgreementEntity agreement) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("agreement_k", agreement.getId());
        snapshot.put("state", enumValue(agreement.getState()));
        snapshot.put("start_date", dateValue(agreement.getStartDate()));
        snapshot.put("first_discount_publishing_date", dateValue(agreement.getFirstDiscountPublishingDate()));
        snapshot.put("reject_reason_msg", agreement.getRejectReasonMessage());
        snapshot.put("image_url", agreement.getImageUrl());
        snapshot.put("assignee", agreement.getBackofficeAssignee());
        snapshot.put("request_approval_time", dateTimeValue(agreement.getRequestApprovalTime()));
        snapshot.put("information_last_update_date", dateValue(agreement.getInformationLastUpdateDate()));
        snapshot.put("organization_name", agreement.getOrganizationName());
        snapshot.put("insert_time", dateTimeValue(agreement.getInsertTime()));
        snapshot.put("update_time", dateTimeValue(agreement.getUpdateTime()));
        snapshot.put("version", agreement.getVersion());
        snapshot.put("entity_type", enumValue(agreement.getEntityType()));
        return snapshot;
    }

    private String enumValue(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private String dateValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String dateTimeValue(Object value) {
        return value == null ? null : value.toString();
    }
}