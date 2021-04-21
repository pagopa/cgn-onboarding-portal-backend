package it.gov.pagopa.cgn.portal.converter.referent;

import it.gov.pagopa.cgnonboardingportal.model.CreateReferent;
import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CreateReferentConverter extends AbstractConverter<ReferentEntity, CreateReferent> {

    @Override
    protected Function<ReferentEntity, CreateReferent> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<CreateReferent, ReferentEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<ReferentEntity, CreateReferent> toDto =
            entity -> {
                CreateReferent dto = new CreateReferent();
                dto.setFirstName(entity.getFirstName());
                dto.setLastName(entity.getLastName());
                dto.setEmailAddress(entity.getEmailAddress());
                dto.setTelephoneNumber(entity.getTelephoneNumber());
                return dto;
            };

    protected  Function<CreateReferent, ReferentEntity> toEntity =
            dto -> {
                ReferentEntity entity = new ReferentEntity();
                entity.setFirstName(dto.getFirstName());
                entity.setLastName(dto.getLastName());
                entity.setEmailAddress(dto.getEmailAddress());
                entity.setTelephoneNumber(dto.getTelephoneNumber());
                return entity;
            };

}