package it.gov.pagopa.converter.profile;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.cgnonboardingportal.model.*;
import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.model.AddressEntity;
import it.gov.pagopa.model.ProfileEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles({"dev"})
class UpdateProfileConverterTest extends BaseTest {

    @Autowired
    private UpdateProfileConverter updateProfileConverter;

    @Test
    void Convert_ConvertProfileEntityOfflineToDTO_ThrowUnsupportedException() {
        ProfileEntity profileEntity = createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        //Not implemented yet
        Assertions.assertThrows(UnsupportedOperationException.class, () -> updateProfileConverter.toDto(profileEntity));
    }

    @Test
    void Convert_ConvertUpdateProfileOnlineDTOToEntity_Ok() {
        UpdateProfile dto = createSampleUpdateProfileWithCommonFields();
        OnlineChannel onlineChannel = new OnlineChannel();
        onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
        onlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        dto.setSalesChannel(onlineChannel);
        ProfileEntity profileEntity = updateProfileConverter.toEntity(dto);

        checkCommonsUpdateProfileAssertions(dto, profileEntity);
        Assertions.assertEquals(SalesChannelEnum.ONLINE, profileEntity.getSalesChannel());
        Assertions.assertEquals(onlineChannel.getWebsiteUrl(), profileEntity.getWebsiteUrl());
    }

    @Test
    void Convert_ConvertUpdateProfileOfflineDTOToEntity_Ok() {
        UpdateProfile dto = createSampleUpdateProfileWithCommonFields();
        OfflineChannel offlineChannel = new OfflineChannel();
        offlineChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
        offlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        offlineChannel.setAddresses(createSampleAddressDto());
        dto.setSalesChannel(offlineChannel);
        ProfileEntity profileEntity = updateProfileConverter.toEntity(dto);

        checkCommonsUpdateProfileAssertions(dto, profileEntity);
        Assertions.assertEquals(SalesChannelEnum.OFFLINE, profileEntity.getSalesChannel());
        Assertions.assertEquals(offlineChannel.getWebsiteUrl(), profileEntity.getWebsiteUrl());
        Assertions.assertNotNull(offlineChannel.getAddresses());
        Assertions.assertNotNull(profileEntity.getAddressList());
        Assertions.assertEquals(offlineChannel.getAddresses().size(), profileEntity.getAddressList().size());

        IntStream.range(0, profileEntity.getAddressList().size()).forEach(idx -> {
            Address dtoAddress = offlineChannel.getAddresses().get(idx);
            AddressEntity entityAddress = profileEntity.getAddressList().get(idx);
            Assertions.assertEquals(dtoAddress.getCity(), entityAddress.getCity());
            Assertions.assertEquals(dtoAddress.getStreet(), entityAddress.getStreet());
            Assertions.assertEquals(dtoAddress.getDistrict(), entityAddress.getDistrict());
            Assertions.assertEquals(dtoAddress.getZipCode(), entityAddress.getZipCode());
        });
    }

    @Test
    void Convert_ConvertUpdateProfileBothDTOToEntity_Ok() {
        UpdateProfile dto = createSampleUpdateProfileWithCommonFields();
        BothChannels bothChannels = new BothChannels();
        bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
        bothChannels.setWebsiteUrl("https://www.pagopa.gov.it/");
        bothChannels.setAddresses(createSampleAddressDto());
        dto.setSalesChannel(bothChannels);
        ProfileEntity profileEntity = updateProfileConverter.toEntity(dto);

        checkCommonsUpdateProfileAssertions(dto, profileEntity);
        Assertions.assertEquals(SalesChannelEnum.BOTH, profileEntity.getSalesChannel());
        Assertions.assertEquals(bothChannels.getWebsiteUrl(), profileEntity.getWebsiteUrl());
        Assertions.assertNotNull(bothChannels.getAddresses());
        Assertions.assertNotNull(profileEntity.getAddressList());
        Assertions.assertEquals(bothChannels.getAddresses().size(), profileEntity.getAddressList().size());

        IntStream.range(0, profileEntity.getAddressList().size()).forEach(idx -> {
            Address dtoAddress = bothChannels.getAddresses().get(idx);
            AddressEntity entityAddress = profileEntity.getAddressList().get(idx);
            Assertions.assertEquals(dtoAddress.getCity(), entityAddress.getCity());
            Assertions.assertEquals(dtoAddress.getStreet(), entityAddress.getStreet());
            Assertions.assertEquals(dtoAddress.getDistrict(), entityAddress.getDistrict());
            Assertions.assertEquals(dtoAddress.getZipCode(), entityAddress.getZipCode());
        });
    }


    private void checkCommonsUpdateProfileAssertions(UpdateProfile dto, ProfileEntity profileEntity) {
        Assertions.assertEquals(dto.getName(), profileEntity.getName());
        Assertions.assertEquals(dto.getDescription(), profileEntity.getDescription());
        Assertions.assertEquals(dto.getPecAddress(), profileEntity.getPecAddress());
        Assertions.assertNotNull(profileEntity.getReferent());
        Assertions.assertNotNull(dto.getReferent());
        Assertions.assertEquals(dto.getReferent().getFirstName(), profileEntity.getReferent().getFirstName());
        Assertions.assertEquals(dto.getReferent().getLastName(), profileEntity.getReferent().getLastName());
        Assertions.assertEquals(dto.getReferent().getEmailAddress(), profileEntity.getReferent().getEmailAddress());
        Assertions.assertEquals(dto.getReferent().getTelephoneNumber(), profileEntity.getReferent().getTelephoneNumber());

    }

}
