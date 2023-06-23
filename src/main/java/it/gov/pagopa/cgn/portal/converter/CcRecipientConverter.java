package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.model.CCRecipientEntity;
import it.gov.pagopa.cgnonboardingportal.model.CcRecipient;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CcRecipientConverter extends AbstractConverter<CCRecipientEntity, CcRecipient> {

    @Override
    protected Function<CCRecipientEntity, CcRecipient> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<CcRecipient, CCRecipientEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<CCRecipientEntity, CcRecipient> toDto =
            entity -> {
                CcRecipient dto = new CcRecipient();
                dto.setEmailAddress(entity.getEmailAddress());
                dto.setProfileId(entity.getProfile().getId().toString());
                return dto;
            };

    protected Function<CcRecipient, CCRecipientEntity> toEntity =
            dto -> {
                CCRecipientEntity entity = new CCRecipientEntity();
                entity.setEmailAddress(dto.getEmailAddress());
                return entity;
            };

}
