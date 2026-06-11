package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.audit.ChangeAuditEvent;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import it.gov.pagopa.cgn.portal.repository.ChangeAuditRepository;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ChangeAuditService {

    private final ChangeAuditRepository changeAuditRepository;
    private final ProfileRepository profileRepository;

    public ChangeAuditService(ChangeAuditRepository changeAuditRepository,
                              ProfileRepository profileRepository) {
        this.changeAuditRepository = changeAuditRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChangeAuditEntity save(ChangeAuditEvent event) {
        ChangeAuditEntity entity = new ChangeAuditEntity();
        entity.setSubjectId(event.getSubjectId());
        entity.setPartnerFullName(resolvePartnerFullName(event));
        entity.setActorRef(event.getActorRef());
        entity.setSubjectType(event.getSubjectType());
        entity.setOperationType(event.getOperationType());
        entity.setInsertTime(OffsetDateTime.now());
        entity.setValue(event.getValue());
        return changeAuditRepository.save(entity);
    }

    private String resolvePartnerFullName(ChangeAuditEvent event) {
        if (event.getSubjectType() != ChangeAuditSubjectTypeEnum.AGREEMENT ||
            event.getOperationType() == ChangeAuditOperationTypeEnum.DELETE) {
            return event.getPartnerFullName();
        }

        return profileRepository.findByAgreementId(event.getSubjectId())
                                .map(ProfileEntity::getFullName)
                                .orElse(event.getPartnerFullName());
    }
}