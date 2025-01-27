package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.referent.CreateReferentConverter;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgnonboardingportal.model.CreateReferent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreateProfileConverter
        extends CommonProfileConverter<ProfileEntity, CreateProfile> {

    private CreateReferentConverter createReferentConverter;

    protected BiFunction<CreateReferent, ProfileEntity, SecondaryReferentEntity> secondaryReferentToEntity = (referentDto, profileEntity) -> {
        SecondaryReferentEntity entity = new SecondaryReferentEntity();
        entity.setFirstName(referentDto.getFirstName());
        entity.setLastName(referentDto.getLastName());
        entity.setEmailAddress(referentDto.getEmailAddress());
        entity.setTelephoneNumber(referentDto.getTelephoneNumber());
        entity.setRole(referentDto.getRole());
        entity.setProfile(profileEntity);
        return entity;
    };

    protected Function<CreateProfile, ProfileEntity> toEntity = dto -> {
        ProfileEntity entity = new ProfileEntity();
        entity.setFullName(dto.getFullName());
        entity.setName(dto.getName());
        entity.setNameEn(dto.getNameEn());
        entity.setNameDe(dto.getNameDe());
        entity.setTaxCodeOrVat(dto.getTaxCodeOrVat());
        entity.setDescription(dto.getDescription());
        entity.setDescriptionEn(dto.getDescriptionEn());
        entity.setDescriptionDe(dto.getDescriptionDe());
        entity.setPecAddress(dto.getPecAddress());
        salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        ReferentEntity referentEntity = createReferentConverter.toEntity(dto.getReferent());
        referentEntity.setProfile(entity);
        entity.setReferent(referentEntity);
        entity.setSecondaryReferentList(Optional.ofNullable(dto.getSecondaryReferents())
                                                .orElse(Collections.emptyList())
                                                .stream()
                                                .map(secondaryReferent -> this.secondaryReferentToEntity.apply(
                                                        secondaryReferent,
                                                        entity))
                                                .collect(Collectors.toCollection(ArrayList::new)));
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        return entity;
    };

    @Autowired
    public CreateProfileConverter(CreateReferentConverter createReferentConverter) {
        this.createReferentConverter = createReferentConverter;
    }

    @Override
    protected Function<ProfileEntity, CreateProfile> toDtoFunction() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Function<CreateProfile, ProfileEntity> toEntityFunction() {
        return toEntity;
    }

}