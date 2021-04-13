package it.gov.pagopa.converter.profile;

import it.gov.pagopa.cgnonboardingportal.model.Profile;
import it.gov.pagopa.converter.referent.ReferentConverter;
import it.gov.pagopa.model.ProfileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ProfileConverter extends CommonProfileConverter<ProfileEntity, Profile> {
    private ReferentConverter referentConverter;
    protected Function<Profile, ProfileEntity> toEntity = (dto) -> {
        ProfileEntity entity = new ProfileEntity();
        entity.setFullName(dto.getFullName());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPecAddress(dto.getPecAddress());
        this.salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        entity.setReferent(this.referentConverter.toEntity(dto.getReferent()));
        return entity;
    };
    protected Function<ProfileEntity, Profile> toDto = (entity) -> {
        Profile dto = new Profile();
        dto.setId(String.valueOf(entity.getId()));
        dto.setTaxCodeOrVat("FAKE");
        dto.setFullName(entity.getFullName());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPecAddress(entity.getPecAddress());
        dto.setReferent(this.referentConverter.toDto(entity.getReferent()));
        dto.setSalesChannel(this.salesChannelToDto.apply(entity));
        return dto;
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
