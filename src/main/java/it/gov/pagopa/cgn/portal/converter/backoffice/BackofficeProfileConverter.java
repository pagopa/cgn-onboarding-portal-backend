package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DocumentType;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Profile;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Referent;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class BackofficeProfileConverter extends AbstractConverter<ProfileEntity, Profile> {

    private static final Map<DocumentTypeEnum, DocumentType> enumMap = new EnumMap<>(DocumentTypeEnum.class);
    static {
        enumMap.put(DocumentTypeEnum.AGREEMENT, DocumentType.AGREEMENT);
        enumMap.put(DocumentTypeEnum.MANIFESTATION_OF_INTEREST, DocumentType.MANIFESTATIONOFINTEREST);
    }

    @Override
    protected Function<ProfileEntity, Profile> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Profile, ProfileEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<ReferentEntity, Referent> toDtoReferent= entity -> {
        Referent dto = new Referent();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setTelephoneNumber(entity.getTelephoneNumber());
        return dto;
    };

    protected Function<ProfileEntity, Profile> toDto =
            entity -> {
                Profile dto = new Profile();
                dto.setAgreementId(entity.getAgreement().getId());
                dto.setId(String.valueOf(entity.getId()));
                dto.setFullName(entity.getFullName());
                dto.setReferent(toDtoReferent.apply(entity.getReferent()));
                return dto;
            };

}
