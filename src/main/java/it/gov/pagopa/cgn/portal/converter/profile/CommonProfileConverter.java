package it.gov.pagopa.cgn.portal.converter.profile;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AddressEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.util.RegexUtils;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class CommonProfileConverter<E, D>
        extends AbstractConverter<E, D> {

    private static final Map<DiscountCodeTypeEnum, DiscountCodeType> discountCodeTypeMap = new EnumMap<>(
            DiscountCodeTypeEnum.class);
    public static final String SALES_CHANNEL_IS_INVALID = "SalesChannel is invalid";

    static {
        discountCodeTypeMap.put(DiscountCodeTypeEnum.API, DiscountCodeType.API);
        discountCodeTypeMap.put(DiscountCodeTypeEnum.STATIC, DiscountCodeType.STATIC);
        discountCodeTypeMap.put(DiscountCodeTypeEnum.LANDINGPAGE, DiscountCodeType.LANDING_PAGE);
        discountCodeTypeMap.put(DiscountCodeTypeEnum.BUCKET, DiscountCodeType.BUCKET);
    }

    protected Function<DiscountCodeTypeEnum, DiscountCodeType> toDtoDiscountCodeTypeEnum = entityEnum -> Optional.ofNullable(
            discountCodeTypeMap.get(entityEnum)).orElseThrow(() -> getInvalidEnumMapping(entityEnum.getCode()));

    protected Function<DiscountCodeType, DiscountCodeTypeEnum> toEntityDiscountCodeTypeEnum = discountCodeType -> discountCodeTypeMap.entrySet()
                                                                                                                                     .stream()
                                                                                                                                     .filter(entry -> entry.getValue()
                                                                                                                                                           .equals(discountCodeType))
                                                                                                                                     .map(Map.Entry::getKey)
                                                                                                                                     .findFirst()
                                                                                                                                     .orElseThrow();

    protected BiConsumer<Coordinates, AddressEntity> setCoordinatesFromDto = (coordinates, addressEntity) -> {
        if (coordinates!=null && coordinates.getLongitude()!=null && coordinates.getLatitude()!=null) {
            addressEntity.setLongitude(coordinates.getLongitude().doubleValue());
            addressEntity.setLatitude(coordinates.getLatitude().doubleValue());
        }
    };

    protected Function<AddressEntity, Coordinates> getCoordinatesFromEntity = addressEntity -> {
        Coordinates coordinates = new Coordinates();
        if (addressEntity.getLongitude()!=null && addressEntity.getLatitude()!=null) {
            coordinates.setLatitude(BigDecimal.valueOf(addressEntity.getLatitude()));
            coordinates.setLongitude(BigDecimal.valueOf(addressEntity.getLongitude()));
        }
        return coordinates;
    };

    protected BiFunction<Address, ProfileEntity, AddressEntity> addressToEntity = (addressDto, profileEntity) -> {
        AddressEntity entity = new AddressEntity();
        entity.setFullAddress(addressDto.getFullAddress());
        entity.setProfile(profileEntity);
        setCoordinatesFromDto.accept(addressDto.getCoordinates(), entity);
        return entity;
    };

    protected BiConsumer<SalesChannel, ProfileEntity> salesChannelConsumer = (salesChannelDto, entity) -> {
        SalesChannelType channelType = salesChannelDto.getChannelType();
        switch (channelType) {
            case ONLINE_CHANNEL:
                if (salesChannelDto instanceof OnlineChannel onlineChannel) {
                    validateWebsiteUrl(onlineChannel.getWebsiteUrl());
                    entity.setSalesChannel(SalesChannelEnum.ONLINE);
                    entity.setWebsiteUrl(onlineChannel.getWebsiteUrl());
                    entity.setDiscountCodeType(toEntityDiscountCodeTypeEnum.apply(onlineChannel.getDiscountCodeType()));
                } else {
                    throw new InvalidRequestException(SALES_CHANNEL_IS_INVALID);
                }
                break;
            case OFFLINE_CHANNEL:
                if (salesChannelDto instanceof OfflineChannel offlineChannel) {
                    if(!StringUtils.isEmpty(offlineChannel.getWebsiteUrl())) {
                        validateWebsiteUrl(offlineChannel.getWebsiteUrl());
                    }
                    entity.setSalesChannel(SalesChannelEnum.OFFLINE);
                    entity.setWebsiteUrl(offlineChannel.getWebsiteUrl());
                    // addressList must be not empty
                    entity.setAddressList(offlineChannel.getAddresses()
                                                              .stream()
                                                              .map(address -> addressToEntity.apply(address, entity))
                                                              .toList());
                    entity.setAllNationalAddresses(offlineChannel.getAllNationalAddresses());
                } else {
                    throw new InvalidRequestException(SALES_CHANNEL_IS_INVALID);
                }
                break;
            case BOTH_CHANNELS:
                if (salesChannelDto instanceof BothChannels bothChannels) {
                    validateWebsiteUrl(bothChannels.getWebsiteUrl());
                    entity.setSalesChannel(SalesChannelEnum.BOTH);
                    entity.setWebsiteUrl(bothChannels.getWebsiteUrl());
                    entity.setAddressList(bothChannels.getAddresses()
                                                      .stream()
                                                      .map(address -> addressToEntity.apply(address, entity))
                                                      .toList());
                    entity.setDiscountCodeType(toEntityDiscountCodeTypeEnum.apply(bothChannels.getDiscountCodeType()));
                    entity.setAllNationalAddresses(bothChannels.getAllNationalAddresses());
                } else {
                    throw new InvalidRequestException(SALES_CHANNEL_IS_INVALID);
                }
                break;
        }
    };

    private void validateWebsiteUrl(String websiteUrl) {
        if(!RegexUtils.checkRulesForInternetUrl(websiteUrl)) {
            throw new InvalidRequestException("website url not valid");
        }
    }

    protected Function<AddressEntity, Address> addressToDto = entity -> {
        Address dto = new Address();
        dto.setFullAddress(entity.getFullAddress());
        dto.setCoordinates(getCoordinatesFromEntity.apply(entity));
        return dto;
    };
    protected Function<ProfileEntity, SalesChannel> salesChannelToDto = entity -> {
        switch (entity.getSalesChannel()) {
            case ONLINE:
                OnlineChannel onlineChannel = new OnlineChannel();
                onlineChannel.setChannelType(SalesChannelType.ONLINE_CHANNEL);
                onlineChannel.setWebsiteUrl(entity.getWebsiteUrl());
                onlineChannel.setDiscountCodeType(toDtoDiscountCodeTypeEnum.apply(entity.getDiscountCodeType()));
                return onlineChannel;
            case OFFLINE:
                OfflineChannel physicalStoreChannel = new OfflineChannel();
                physicalStoreChannel.setChannelType(SalesChannelType.OFFLINE_CHANNEL);
                physicalStoreChannel.setWebsiteUrl(entity.getWebsiteUrl());
                physicalStoreChannel.setAddresses(entity.getAddressList()
                                                        .stream()
                                                        .sorted(getAddressComparator())
                                                        .map(addressToDto)
                                                        .toList());
                physicalStoreChannel.setAllNationalAddresses(entity.getAllNationalAddresses());
                return physicalStoreChannel;
            case BOTH:
                BothChannels bothChannels = new BothChannels();
                bothChannels.setChannelType(SalesChannelType.BOTH_CHANNELS);
                bothChannels.setWebsiteUrl(entity.getWebsiteUrl());
                bothChannels.setAddresses(entity.getAddressList()
                                                .stream()
                                                .sorted(getAddressComparator())
                                                .map(addressToDto)
                                                .toList());
                bothChannels.setDiscountCodeType(toDtoDiscountCodeTypeEnum.apply(entity.getDiscountCodeType()));
                bothChannels.setAllNationalAddresses(entity.getAllNationalAddresses());
                return bothChannels;
            default:
                throw new IllegalArgumentException("Sales Channel not mapped");
        }

    };

    private Comparator<AddressEntity> getAddressComparator() {
        return Comparator.comparing(AddressEntity::getId);
    }

}
