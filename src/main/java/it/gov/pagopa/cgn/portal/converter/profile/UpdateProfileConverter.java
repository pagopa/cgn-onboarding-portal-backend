package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.referent.UpdateReferentConverter;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UpdateProfileConverter
        extends CommonProfileConverter<ProfileEntity, UpdateProfile> {

    private UpdateReferentConverter updateReferentConverter;

    protected BiFunction<UpdateReferent, ProfileEntity, SecondaryReferentEntity> secondaryReferentToEntity = (referentDto, profileEntity) -> {
        SecondaryReferentEntity entity = new SecondaryReferentEntity();
        entity.setFirstName(referentDto.getFirstName());
        entity.setLastName(referentDto.getLastName());
        entity.setEmailAddress(referentDto.getEmailAddress());
        entity.setTelephoneNumber(referentDto.getTelephoneNumber());
        entity.setRole(referentDto.getRole());
        entity.setProfile(profileEntity);
        return entity;
    };

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
        entity.setSecondaryReferentList(Optional.ofNullable(dto.getSecondaryReferents())
                                                .orElse(Collections.emptyList())
                                                .stream()
                                                .map(secondaryReferent -> this.secondaryReferentToEntity.apply(
                                                        secondaryReferent,
                                                        entity))
                                                .collect(Collectors.toCollection(ArrayList::new)));
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        return entity;
    };

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

}