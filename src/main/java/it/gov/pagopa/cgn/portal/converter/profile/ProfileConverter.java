package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.CcRecipientConverter;
import it.gov.pagopa.cgn.portal.converter.referent.ReferentConverter;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProfileConverter extends CommonProfileConverter<ProfileEntity, Profile> {
    private ReferentConverter referentConverter;
    private CcRecipientConverter ccRecipientConverter;
    protected Function<Profile, ProfileEntity> toEntity = dto -> {
        ProfileEntity entity = new ProfileEntity();
        entity.setFullName(dto.getFullName());
        entity.setName(dto.getName());
        entity.setNameEn(dto.getNameEn());
        entity.setNameDe(dto.getNameDe());
        entity.setDescription(dto.getDescription());
        entity.setDescriptionEn(dto.getDescriptionEn());
        entity.setDescriptionDe(dto.getDescriptionDe());
        entity.setPecAddress(dto.getPecAddress());
        this.salesChannelConsumer.accept(dto.getSalesChannel(), entity);
        entity.setReferent(this.referentConverter.toEntity(dto.getReferent()));
        entity.setTelephoneNumber(dto.getTelephoneNumber());
        entity.setLegalRepresentativeFullName(dto.getLegalRepresentativeFullName());
        entity.setLegalOffice(dto.getLegalOffice());
        entity.setLegalRepresentativeTaxCode(dto.getLegalRepresentativeTaxCode());
        entity.setSupportType(toEntitySupportTypeEnum.apply(dto.getSupportType()));
        entity.setSupportValue(dto.getSupportValue());
        entity.setCcRecipientList(dto.getCcRecipients().stream()
                .map(ccRecipientDto-> this.ccRecipientConverter.toEntity(ccRecipientDto)).collect(Collectors.toList()));
        return entity;
    };
    protected Function<ProfileEntity, Profile> toDto = entity -> {
        Profile profile = new Profile();
        profile.setId(String.valueOf(entity.getId()));
        profile.setTaxCodeOrVat(entity.getTaxCodeOrVat());
        profile.setFullName(entity.getFullName());
        profile.setName(entity.getName());
        profile.setNameEn(entity.getNameEn());
        profile.setNameDe(entity.getNameDe());
        profile.setDescription(entity.getDescription());
        profile.setDescriptionEn(entity.getDescriptionEn());
        profile.setDescriptionDe(entity.getDescriptionDe());
        profile.setPecAddress(entity.getPecAddress());
        profile.setReferent(this.referentConverter.toDto(entity.getReferent()));
        profile.setSalesChannel(this.salesChannelToDto.apply(entity));
        profile.setAgreementId(entity.getAgreement().getId());
        profile.setTelephoneNumber(entity.getTelephoneNumber());
        profile.setLegalRepresentativeFullName(entity.getLegalRepresentativeFullName());
        profile.setLegalOffice(entity.getLegalOffice());
        profile.setLegalRepresentativeTaxCode(entity.getLegalRepresentativeTaxCode());
        profile.setSupportType(toDtoSupportTypeEnum.apply(entity.getSupportType()));
        profile.setSupportValue(entity.getSupportValue());
        profile.setCcRecipients(entity.getCcRecipientList().stream()
                .map(ccRecipientEntity -> this.ccRecipientConverter.toDto(ccRecipientEntity)).collect(Collectors.toList()));
        return profile;
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
