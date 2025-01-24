package it.gov.pagopa.cgn.portal.converter.referent;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.model.SecondaryReferentEntity;
import it.gov.pagopa.cgnonboardingportal.model.Referent;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class SecondaryReferentConverter
        extends AbstractConverter<SecondaryReferentEntity, Referent> {

    protected Function<SecondaryReferentEntity, Referent> toDto = entity -> {
        Referent dto = new Referent();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setTelephoneNumber(entity.getTelephoneNumber());
        dto.setRole(entity.getRole());
        return dto;
    };
    protected Function<Referent, SecondaryReferentEntity> toEntity = dto -> {
        SecondaryReferentEntity entity = new SecondaryReferentEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setRole(dto.getRole());
        return entity;
    };

    @Override
    protected Function<SecondaryReferentEntity, Referent> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Referent, SecondaryReferentEntity> toEntityFunction() {
        return toEntity;
    }

}