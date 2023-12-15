package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SupportTypeEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class BackofficeAgreementConverter extends AbstractConverter<AgreementEntity, Agreement> {

    private static final Map<String, AgreementStateEnum> enumMap = new HashMap<>(4);
    private static final Map<EntityTypeEnum, EntityType> backofficeEntityTypeEnumMap = new EnumMap<>(EntityTypeEnum.class);


    static {
        enumMap.put(AgreementState.APPROVEDAGREEMENT.getValue(), AgreementStateEnum.APPROVED);
        enumMap.put(AgreementState.PENDINGAGREEMENT.getValue(), AgreementStateEnum.PENDING);
        enumMap.put(AgreementState.REJECTEDAGREEMENT.getValue(), AgreementStateEnum.REJECTED);
        enumMap.put(AgreementState.ASSIGNEDAGREEMENT.getValue(), AgreementStateEnum.PENDING);
        backofficeEntityTypeEnumMap.put(
                EntityTypeEnum.PRIVATE, EntityType.PRIVATE);
        backofficeEntityTypeEnumMap.put(
                EntityTypeEnum.PUBLIC_ADMINISTRATION, EntityType.PUBLICADMINISTRATION);
    }

    private BackofficeDiscountConverter discountConverter;
    private BackofficeDocumentConverter documentConverter;
    private BackofficeProfileConverter profileConverter;


    @Autowired
    public BackofficeAgreementConverter(BackofficeDiscountConverter discountConverter,
                                        BackofficeDocumentConverter documentConverter,
                                        BackofficeProfileConverter profileConverter) {
        this.discountConverter = discountConverter;
        this.documentConverter = documentConverter;
        this.profileConverter = profileConverter;
    }

    @Override
    protected Function<AgreementEntity, Agreement> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Agreement, AgreementEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Agreements getAgreementFromPage(Page<AgreementEntity> agreementEntityPage) {
        Collection<Agreement> dtoCollection = toDtoCollection(agreementEntityPage.getContent());
        Agreements agreements = new Agreements();
        agreements.setItems(new ArrayList<>(dtoCollection));
        agreements.setTotal((int) agreementEntityPage.getTotalElements());
        return agreements;
    }

    public static boolean isAgreementStateIsAssigned(String statusDtoCode) {
        AgreementStateEnum agreementStateEnum = getAgreementStateEnumFromDtoCode(statusDtoCode);
        return agreementStateEnum.equals(AgreementStateEnum.PENDING) &&
                AgreementState.ASSIGNEDAGREEMENT.getValue().equals(statusDtoCode);
    }

    public static AgreementStateEnum getAgreementStateEnumFromDtoCode(String statusDtoCode) {
        return Optional.ofNullable(enumMap.get(statusDtoCode))
                .orElseThrow(() -> getInvalidEnumMapping(statusDtoCode));

    }

    public static EntityType getEntityTypeFromEntityTypeEnum(EntityTypeEnum etEnum) {
        return Optional.ofNullable(backofficeEntityTypeEnumMap.get(etEnum))
                .orElseThrow(() -> getInvalidEnumMapping(etEnum.getCode()));

    }

    public EntityTypeEnum toEntityEntityTypeEnum(EntityType entityType) {
        return backofficeEntityTypeEnumMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(entityType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    private final Function<AgreementEntity, Agreement> toDtoWithStatusFilled = entity -> {
        Agreement dto;
        if (entity.getState() == AgreementStateEnum.PENDING) {
            if (StringUtils.isBlank(entity.getBackofficeAssignee())) {
                dto = new PendingAgreement();
                dto.setState(AgreementState.PENDINGAGREEMENT);
            } else {
                AssignedAgreement assignedAgreement = new AssignedAgreement();
                Assignee assignee = new Assignee();
                assignee.setFullName(entity.getBackofficeAssignee());
                assignedAgreement.setAssignee(assignee);
                dto = assignedAgreement;
                dto.setState(AgreementState.ASSIGNEDAGREEMENT);
            }
        } else {
            throw new CGNException("Enum mapping not found for " + entity.getState());
        }
        return dto;
    };

    protected Function<AgreementEntity, Agreement> toDto =
            entity -> {
                Agreement dto = toDtoWithStatusFilled.apply(entity);
                dto.setId(entity.getId());
                dto.setEntityType(getEntityTypeFromEntityTypeEnum(entity.getEntityType()));
                if (entity.getRequestApprovalTime() != null) {
                    dto.setRequestDate(entity.getRequestApprovalTime().toLocalDate());
                }
                dto.setDiscounts((List<Discount>) discountConverter.toDtoCollection(entity.getDiscountList()));
                dto.setDocuments((List<Document>) documentConverter.toDtoCollection(entity.getDocumentList()));
                dto.setProfile(profileConverter.toDto(entity.getProfile()));
                return dto;
            };
}
