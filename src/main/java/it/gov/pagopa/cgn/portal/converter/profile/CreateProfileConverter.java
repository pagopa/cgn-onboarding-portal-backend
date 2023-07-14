package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.referent.CreateReferentConverter;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgnonboardingportal.model.CreateReferent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreateProfileConverter extends CommonProfileConverter<ProfileEntity, CreateProfile> {

    private CreateReferentConverter createReferentConverter;

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
        entity.setSecondaryReferentList(dto.getSecondaryReferents().stream()
                .map(secondaryReferent -> this.createReferentToSecondaryReferentEntity.apply(secondaryReferent, entity))
                .collect(Collectors.toList()));
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setSupportType(toEntitySupportTypeEnum.apply(dto.getSupportType()));
        entity.setSupportValue(dto.getSupportValue());

        return entity;
    };

    private final BiFunction<CreateReferent, ProfileEntity, SecondaryReferentEntity> createReferentToSecondaryReferentEntity = (createReferent, profileEntity) -> {
        SecondaryReferentEntity secondaryReferentEntity = (SecondaryReferentEntity) this.createReferentConverter.toEntity(createReferent);
        secondaryReferentEntity.setProfile(profileEntity);
        return secondaryReferentEntity;
    };



}