package it.gov.pagopa.cgn.portal.converter.backoffice.approved;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.AddressEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BackofficeApprovedAgreementProfileConverter
        extends AbstractConverter<ProfileEntity, ApprovedAgreementProfile> {

    private static final Map<DiscountCodeTypeEnum, DiscountCodeType> discountCodeTypeMap = new EnumMap<>(
            DiscountCodeTypeEnum.class);

    static {
        discountCodeTypeMap.put(DiscountCodeTypeEnum.API, DiscountCodeType.API);
        discountCodeTypeMap.put(DiscountCodeTypeEnum.STATIC, DiscountCodeType.STATIC);
        discountCodeTypeMap.put(DiscountCodeTypeEnum.LANDINGPAGE, DiscountCodeType.LANDINGPAGE);
        discountCodeTypeMap.put(DiscountCodeTypeEnum.BUCKET, DiscountCodeType.BUCKET);
    }

    protected Function<DiscountCodeTypeEnum, DiscountCodeType> toDtoDiscountCodeTypeEnum
            = entityEnum -> Optional.ofNullable(discountCodeTypeMap.get(entityEnum))
                                    .orElseThrow(() -> getInvalidEnumMapping(entityEnum.getCode()));

    @Override
    protected Function<ProfileEntity, ApprovedAgreementProfile> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<ApprovedAgreementProfile, ProfileEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<ReferentEntity, ApprovedAgreementReferent> toDtoReferent = entity -> {
        ApprovedAgreementReferent dto = new ApprovedAgreementReferent();
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setTelephoneNumber(entity.getTelephoneNumber());
        dto.setRole(entity.getRole());
        return dto;
    };

    protected Function<List<AddressEntity>, List<String>> addressToDto = entityList -> entityList.stream()
                                                                                                 .map(AddressEntity::getFullAddress)
                                                                                                 .collect(Collectors.toList());

    protected Function<ProfileEntity, SalesChannel> salesChannelToDto = entity -> {
        switch (entity.getSalesChannel()) {
            case ONLINE:
                OnlineChannel onlineChannel = new OnlineChannel();
                onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
                onlineChannel.setWebsiteUrl(entity.getWebsiteUrl());
                onlineChannel.setDiscountCodeType(toDtoDiscountCodeTypeEnum.apply(entity.getDiscountCodeType()));
                return onlineChannel;
            case OFFLINE:
                OfflineChannel physicalStoreChannel = new OfflineChannel();
                physicalStoreChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
                physicalStoreChannel.setWebsiteUrl(entity.getWebsiteUrl());
                physicalStoreChannel.setAddresses(addressToDto.apply(entity.getAddressList()));
                return physicalStoreChannel;
            case BOTH:
                BothChannels bothChannels = new BothChannels();
                bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
                bothChannels.setWebsiteUrl(entity.getWebsiteUrl());
                bothChannels.setAddresses(addressToDto.apply(entity.getAddressList()));
                bothChannels.setDiscountCodeType(toDtoDiscountCodeTypeEnum.apply(entity.getDiscountCodeType()));
                return bothChannels;
            default:
                throw new IllegalArgumentException("Sales Channel not mapped");
        }
    };

    protected Function<ProfileEntity, ApprovedAgreementProfile> toDto = entity -> {
        ApprovedAgreementProfile dto = new ApprovedAgreementProfile();
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setImageUrl(entity.getAgreement().getImageUrl());
        OffsetDateTime updateDateTime;
        updateDateTime = entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getInsertTime();
        dto.setLastUpateDate(LocalDate.from(updateDateTime));
        dto.setFullName(entity.getFullName());
        dto.setTaxCodeOrVat(entity.getTaxCodeOrVat());
        dto.setPecAddress(entity.getPecAddress());
        dto.setLegalOffice(entity.getLegalOffice());
        dto.setTelephoneNumber(entity.getTelephoneNumber());
        dto.setLegalRepresentativeFullName(entity.getLegalRepresentativeFullName());
        dto.setLegalRepresentativeTaxCode(entity.getLegalRepresentativeTaxCode());
        dto.setReferent(toDtoReferent.apply(entity.getReferent()));
        dto.setSalesChannel(salesChannelToDto.apply(entity));
        return dto;
    };
}
