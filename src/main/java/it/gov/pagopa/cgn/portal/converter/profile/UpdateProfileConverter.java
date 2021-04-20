package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.cgn.portal.converter.referent.UpdateReferentConverter;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

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
        entity.setDescription(dto.getDescription());
        entity.setPecAddress(dto.getPecAddress());
        salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        ReferentEntity referentEntity = updateReferentConverter.toEntity(dto.getReferent());
        referentEntity.setProfile(entity);
        entity.setReferent(referentEntity);
        return entity;
    };


}