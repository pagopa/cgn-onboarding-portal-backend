package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgn.portal.converter.referent.CreateReferentConverter;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

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
        entity.setDescription(dto.getDescription());
        entity.setPecAddress(dto.getPecAddress());
        salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        ReferentEntity referentEntity = createReferentConverter.toEntity(dto.getReferent());
        referentEntity.setProfile(entity);
        entity.setReferent(referentEntity);
        return entity;
    };
}