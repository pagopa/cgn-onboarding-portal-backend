package it.gov.pagopa.converter.profile;

import it.gov.pagopa.cgnonboardingportal.model.*;
import it.gov.pagopa.converter.AbstractConverter;
import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.AddressEntity;
import it.gov.pagopa.model.ProfileEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CommonProfileConverter<E, D> extends AbstractConverter<E, D> {

    protected Function<Address, AddressEntity> addressToEntity = dto -> {
        AddressEntity entity = new AddressEntity();
        entity.setStreet(dto.getStreet());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setZipCode(dto.getZipCode());
        return entity;
    };

    protected Function<AddressEntity, Address> addressToDto = entity -> {
        Address dto = new Address();
        dto.setCity(entity.getCity());
        dto.setStreet(entity.getStreet());
        dto.setDistrict(entity.getDistrict());
        dto.setZipCode(entity.getZipCode());
        return dto;
    };

    protected Function<ProfileEntity, SalesChannel> salesChannelToDto = entity -> {
        switch (entity.getSalesChannel()) {
            case ONLINE:
                OnlineChannel onlineChannel = new OnlineChannel();
                onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
                onlineChannel.setWebsiteUrl(entity.getWebsiteUrl());
                return onlineChannel;
            case OFFLINE:
                OfflineChannel physicalStoreChannel = new OfflineChannel();
                physicalStoreChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
                physicalStoreChannel.setWebsiteUrl(entity.getWebsiteUrl());
                if (CollectionUtils.isEmpty(entity.getAddressList())) {
                    throw new InvalidRequestException("OfflineChannel must have at least on address");
                } else {
                    physicalStoreChannel.setAddresses(
                            entity.getAddressList().stream().map(addressToDto).collect(Collectors.toList()));
                }
                return physicalStoreChannel;
            case BOTH:
                BothChannels bothChannels = new BothChannels();
                bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
                bothChannels.setWebsiteUrl(entity.getWebsiteUrl());
                if (CollectionUtils.isEmpty(entity.getAddressList())) {
                    throw new InvalidRequestException("BothChannels must have at least on address");
                } else {
                    bothChannels.setAddresses(
                            entity.getAddressList().stream().map(addressToDto).collect(Collectors.toList()));
                }
                return bothChannels;
            default:
                throw new IllegalArgumentException("Sales Channel not mapped");
        }

    };


    protected BiConsumer<SalesChannel, ProfileEntity> salesChannelConsumer = (salesChannelDto, entity) -> {
        SalesChannelType channelType = salesChannelDto.getChannelType();
        switch (channelType) {
            case ONLINECHANNEL:
                if (salesChannelDto instanceof OnlineChannel) {
                    OnlineChannel onlineChannel = (OnlineChannel) salesChannelDto;
                    entity.setSalesChannel(SalesChannelEnum.ONLINE);
                    if (StringUtils.isBlank(onlineChannel.getWebsiteUrl())) {   //todo implement test to understand if must implement manually
                        throw new InvalidRequestException("OnlineChannel must have website Url");
                    }
                    entity.setWebsiteUrl(onlineChannel.getWebsiteUrl());
                } else {
                    throw new InvalidRequestException("SalesChannel is invalid");
                }
                break;
            case OFFLINECHANNEL:
                if (salesChannelDto instanceof OfflineChannel) {
                    OfflineChannel physicalStoreChannel = (OfflineChannel) salesChannelDto;
                    entity.setSalesChannel(SalesChannelEnum.OFFLINE);
                    entity.setWebsiteUrl(physicalStoreChannel.getWebsiteUrl());
                    if (CollectionUtils.isEmpty(physicalStoreChannel.getAddresses())) {
                        throw new InvalidRequestException("OfflineChannel must have at least on address");
                    }
                    entity.setAddressList(
                            physicalStoreChannel.getAddresses().stream()
                                    .map(address -> addressToEntity.apply(address))
                                    .collect(Collectors.toList()));
                } else {
                    throw new InvalidRequestException("SalesChannel is invalid");
                }
                break;
            case BOTHCHANNELS:
                if (salesChannelDto instanceof BothChannels) {
                    BothChannels bothChannels = (BothChannels) salesChannelDto;
                    entity.setSalesChannel(SalesChannelEnum.BOTH);
                    if (StringUtils.isBlank(bothChannels.getWebsiteUrl())) {   //todo implement test to understand if must implement manually
                        throw new InvalidRequestException("OnlineChannel must have website Url");
                    }
                    entity.setWebsiteUrl(bothChannels.getWebsiteUrl());
                    if (CollectionUtils.isEmpty(bothChannels.getAddresses())) {
                        throw new InvalidRequestException("BothChannels must have at least on address");
                    }
                    entity.setAddressList(
                            bothChannels.getAddresses().stream()
                                    .map(address -> addressToEntity.apply(address))
                                    .collect(Collectors.toList()));
                } else {
                    throw new InvalidRequestException("SalesChannel is invalid");
                }
                break;
        }
    };
}

