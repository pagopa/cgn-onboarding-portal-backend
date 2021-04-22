package it.gov.pagopa.cgn.portal.converter.referent;

import it.gov.pagopa.cgnonboardingportal.model.Referent;
import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ReferentConverter extends AbstractConverter<ReferentEntity, Referent> {

    @Override
    protected Function<ReferentEntity, Referent> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Referent, ReferentEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<ReferentEntity, Referent> toDto =
            entity -> {
                Referent dto = new Referent();
                dto.setFirstName(entity.getFirstName());
                dto.setLastName(entity.getLastName());
                dto.setEmailAddress(entity.getEmailAddress());
                dto.setTelephoneNumber(entity.getTelephoneNumber());
                dto.setRole(entity.getRole());
                return dto;
            };

    protected Function<Referent, ReferentEntity> toEntity =
            dto -> {
                ReferentEntity entity = new ReferentEntity();
                entity.setFirstName(dto.getFirstName());
                entity.setLastName(dto.getLastName());
                entity.setEmailAddress(dto.getEmailAddress());
                entity.setTelephoneNumber(dto.getTelephoneNumber());
                entity.setRole(dto.getRole());
                return entity;
            };

}