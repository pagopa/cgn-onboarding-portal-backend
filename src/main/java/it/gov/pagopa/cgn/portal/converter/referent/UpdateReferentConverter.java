package it.gov.pagopa.cgn.portal.converter.referent;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UpdateReferentConverter
        extends AbstractConverter<ReferentEntity, UpdateReferent> {

    protected Function<ReferentEntity, UpdateReferent> toDto = entity -> {
        UpdateReferent dto = new UpdateReferent();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setTelephoneNumber(entity.getTelephoneNumber());
        dto.setRole(entity.getRole());
        return dto;
    };
    protected Function<UpdateReferent, ReferentEntity> toEntity = dto -> {
        ReferentEntity entity = new ReferentEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setRole(dto.getRole());
        return entity;
    };

    @Override
    protected Function<ReferentEntity, UpdateReferent> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<UpdateReferent, ReferentEntity> toEntityFunction() {
        return toEntity;
    }

}