package it.gov.pagopa.converter.referent;

import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import it.gov.pagopa.converter.AbstractConverter;
import it.gov.pagopa.model.ReferentEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UpdateReferentConverter extends AbstractConverter<ReferentEntity, UpdateReferent> {

    @Override
    protected Function<ReferentEntity, UpdateReferent> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<UpdateReferent, ReferentEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<ReferentEntity, UpdateReferent> toDto =
            entity -> {
                UpdateReferent dto = new UpdateReferent();
                dto.setFirstName(entity.getFirstName());
                dto.setLastName(entity.getLastName());
                dto.setEmailAddress(entity.getEmailAddress());
                dto.setTelephoneNumber(entity.getTelephoneNumber());
                return dto;
            };

    protected  Function<UpdateReferent, ReferentEntity> toEntity =
            dto -> {
                ReferentEntity entity = new ReferentEntity();
                entity.setFirstName(dto.getFirstName());
                entity.setLastName(dto.getLastName());
                entity.setEmailAddress(dto.getEmailAddress());
                entity.setTelephoneNumber(dto.getTelephoneNumber());
                return entity;
            };

}