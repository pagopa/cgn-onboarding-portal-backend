package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.referent.UpdateReferentConverter;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UpdateProfileConverter extends CommonProfileConverter<ProfileEntity, UpdateProfile> {

    private UpdateReferentConverter updateReferentConverter;

    @Autowired
    public UpdateProfileConverter(UpdateReferentConverter updateReferentConverter) {
        this.updateReferentConverter = updateReferentConverter;
    }

    @Override
    protected Function<ProfileEntity, UpdateProfile> toDtoFunction() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Function<UpdateProfile, ProfileEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<UpdateProfile, ProfileEntity> toEntity = dto -> {
        ProfileEntity entity = new ProfileEntity();
        entity.setName(dto.getName());
        entity.setNameEn(dto.getNameEn());
        entity.setNameDe(dto.getNameDe());
        entity.setDescription(dto.getDescription());
        entity.setDescriptionEn(dto.getDescriptionEn());
        entity.setDescriptionDe(dto.getDescriptionDe());
        entity.setPecAddress(dto.getPecAddress());
        salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        ReferentEntity referentEntity = updateReferentConverter.toEntity(dto.getReferent());
        referentEntity.setProfile(entity);
        entity.setReferent(referentEntity);
        entity.setSecondaryReferentList(dto.getSecondaryReferents().stream()
                .map(secondaryReferent -> this.updateReferentToSecondaryReferentEntity.apply(secondaryReferent, entity))
                .collect(Collectors.toList()));
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        entity.setSupportType(toEntitySupportTypeEnum.apply(dto.getSupportType()));
        entity.setSupportValue(dto.getSupportValue());
        return entity;
    };

    private final BiFunction<UpdateReferent, ProfileEntity, SecondaryReferentEntity> updateReferentToSecondaryReferentEntity = (updateReferent, profileEntity) -> {
        SecondaryReferentEntity secondaryReferentEntity = (SecondaryReferentEntity) this.updateReferentConverter.toEntity(updateReferent);
        secondaryReferentEntity.setProfile(profileEntity);
        return secondaryReferentEntity;
    };



}