package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.referent.UpdateReferentConverter;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
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
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        entity.setSupportType(toEntitySupportTypeEnum.apply(dto.getSupportType()));
        entity.setSupportValue(dto.getSupportValue());
        return entity;
    };


}