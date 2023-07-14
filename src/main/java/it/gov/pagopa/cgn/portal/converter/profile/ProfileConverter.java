package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.referent.ReferentConverter;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProfileConverter extends CommonProfileConverter<ProfileEntity, Profile> {
    private ReferentConverter referentConverter;
    protected Function<Profile, ProfileEntity> toEntity = dto -> {
        ProfileEntity entity = new ProfileEntity();
        entity.setFullName(dto.getFullName());
        entity.setName(dto.getName());
        entity.setNameEn(dto.getNameEn());
        entity.setNameDe(dto.getNameDe());
        entity.setDescription(dto.getDescription());
        entity.setDescriptionEn(dto.getDescriptionEn());
        entity.setDescriptionDe(dto.getDescriptionDe());
        entity.setPecAddress(dto.getPecAddress());
        this.salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        entity.setReferent(this.referentConverter.toEntity(dto.getReferent()));
        entity.setSecondaryReferentList(dto.getSecondaryReferents().stream()
                .map(secondaryReferent-> (SecondaryReferentEntity)this.referentConverter.toEntity(secondaryReferent))
                .collect(Collectors.toList()));
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        entity.setSupportType(toEntitySupportTypeEnum.apply(dto.getSupportType()));
        entity.setSupportValue(dto.getSupportValue());
        return entity;
    };
    protected Function<ProfileEntity, Profile> toDto = entity -> {
        Profile profile = new Profile();
        profile.setId(String.valueOf(entity.getId()));
        profile.setTaxCodeOrVat(entity.getTaxCodeOrVat());
        profile.setFullName(entity.getFullName());
        profile.setName(entity.getName());
        profile.setNameEn(entity.getNameEn());
        profile.setNameDe(entity.getNameDe());
        profile.setDescription(entity.getDescription());
        profile.setDescriptionEn(entity.getDescriptionEn());
        profile.setDescriptionDe(entity.getDescriptionDe());
        profile.setPecAddress(entity.getPecAddress());
        profile.setReferent(this.referentConverter.toDto(entity.getReferent()));
        profile.secondaryReferents(entity.getSecondaryReferentList().stream()
                .map(secondaryReferent-> this.referentConverter.toDto(secondaryReferent)).collect(Collectors.toList()));
        profile.setSalesChannel(this.salesChannelToDto.apply(entity));
        profile.setAgreementId(entity.getAgreement().getId());
        profile.setTelephoneNumber(entity.getTelephoneNumber());
        profile.setLegalRepresentativeFullName(entity.getLegalRepresentativeFullName());
        profile.setLegalOffice(entity.getLegalOffice());
        profile.setLegalRepresentativeTaxCode(entity.getLegalRepresentativeTaxCode());
        profile.setSupportType(toDtoSupportTypeEnum.apply(entity.getSupportType()));
        profile.setSupportValue(entity.getSupportValue());
        return profile;
    };

    @Autowired
    public ProfileConverter(ReferentConverter referentConverter) {
        this.referentConverter = referentConverter;
    }

    protected Function<ProfileEntity, Profile> toDtoFunction() {
        return this.toDto;
    }

    protected Function<Profile, ProfileEntity> toEntityFunction() {
        return this.toEntity;
    }
}
