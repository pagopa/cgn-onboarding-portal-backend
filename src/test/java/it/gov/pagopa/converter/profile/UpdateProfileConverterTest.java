package it.gov.pagopa.converter.profile;

import it.gov.pagopa.cgn.TestUtils;
import it.gov.pagopa.cgnonboardingportal.model.*;
import it.gov.pagopa.converter.referent.UpdateReferentConverter;
import it.gov.pagopa.enums.SalesChannelEnum;
import it.gov.pagopa.model.AddressEntity;
import it.gov.pagopa.model.ProfileEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.stream.IntStream;
@RunWith(SpringRunner.class)
public class UpdateProfileConverterTest {


    private UpdateProfileConverter updateProfileConverter = new UpdateProfileConverter(new UpdateReferentConverter());

    @Test
    public void Convert_ConvertProfileEntityOfflineToDTO_ThrowUnsupportedException() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        //Not implemented yet
        Assert.assertThrows(UnsupportedOperationException.class, () -> updateProfileConverter.toDto(profileEntity));
    }

    @Test
    public void Convert_ConvertUpdateProfileOnlineDTOToEntity_Ok() {
        UpdateProfile dto = TestUtils.createSampleUpdateProfileWithCommonFields();
        OnlineChannel onlineChannel = new OnlineChannel();
        onlineChannel.setChannelType(SalesChannelType.ONLINECHANNEL);
        onlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        dto.setSalesChannel(onlineChannel);
        ProfileEntity profileEntity = updateProfileConverter.toEntity(dto);

        checkCommonsUpdateProfileAssertions(dto, profileEntity);
        Assert.assertEquals(SalesChannelEnum.ONLINE, profileEntity.getSalesChannel());
        Assert.assertEquals(onlineChannel.getWebsiteUrl(), profileEntity.getWebsiteUrl());
    }

    @Test
    public void Convert_ConvertUpdateProfileOfflineDTOToEntity_Ok() {
        UpdateProfile dto = TestUtils.createSampleUpdateProfileWithCommonFields();
        OfflineChannel offlineChannel = new OfflineChannel();
        offlineChannel.setChannelType(SalesChannelType.OFFLINECHANNEL);
        offlineChannel.setWebsiteUrl("https://www.pagopa.gov.it/");
        offlineChannel.setAddresses(TestUtils.createSampleAddressDto());
        dto.setSalesChannel(offlineChannel);
        ProfileEntity profileEntity = updateProfileConverter.toEntity(dto);

        checkCommonsUpdateProfileAssertions(dto, profileEntity);
        Assert.assertEquals(SalesChannelEnum.OFFLINE, profileEntity.getSalesChannel());
        Assert.assertEquals(offlineChannel.getWebsiteUrl(), profileEntity.getWebsiteUrl());
        Assert.assertNotNull(offlineChannel.getAddresses());
        Assert.assertNotNull(profileEntity.getAddressList());
        Assert.assertEquals(offlineChannel.getAddresses().size(), profileEntity.getAddressList().size());

        IntStream.range(0, profileEntity.getAddressList().size()).forEach(idx -> {
            Address dtoAddress = offlineChannel.getAddresses().get(idx);
            AddressEntity entityAddress = profileEntity.getAddressList().get(idx);
            Assert.assertEquals(dtoAddress.getCity(), entityAddress.getCity());
            Assert.assertEquals(dtoAddress.getStreet(), entityAddress.getStreet());
            Assert.assertEquals(dtoAddress.getDistrict(), entityAddress.getDistrict());
            Assert.assertEquals(dtoAddress.getZipCode(), entityAddress.getZipCode());
        });
    }

    @Test
    public void Convert_ConvertUpdateProfileBothDTOToEntity_Ok() {
        UpdateProfile dto = TestUtils.createSampleUpdateProfileWithCommonFields();
        BothChannels bothChannels = new BothChannels();
        bothChannels.setChannelType(SalesChannelType.BOTHCHANNELS);
        bothChannels.setWebsiteUrl("https://www.pagopa.gov.it/");
        bothChannels.setAddresses(TestUtils.createSampleAddressDto());
        dto.setSalesChannel(bothChannels);
        ProfileEntity profileEntity = updateProfileConverter.toEntity(dto);

        checkCommonsUpdateProfileAssertions(dto, profileEntity);
        Assert.assertEquals(SalesChannelEnum.BOTH, profileEntity.getSalesChannel());
        Assert.assertEquals(bothChannels.getWebsiteUrl(), profileEntity.getWebsiteUrl());
        Assert.assertNotNull(bothChannels.getAddresses());
        Assert.assertNotNull(profileEntity.getAddressList());
        Assert.assertEquals(bothChannels.getAddresses().size(), profileEntity.getAddressList().size());

        IntStream.range(0, profileEntity.getAddressList().size()).forEach(idx -> {
            Address dtoAddress = bothChannels.getAddresses().get(idx);
            AddressEntity entityAddress = profileEntity.getAddressList().get(idx);
            Assert.assertEquals(dtoAddress.getCity(), entityAddress.getCity());
            Assert.assertEquals(dtoAddress.getStreet(), entityAddress.getStreet());
            Assert.assertEquals(dtoAddress.getDistrict(), entityAddress.getDistrict());
            Assert.assertEquals(dtoAddress.getZipCode(), entityAddress.getZipCode());
        });
    }


    private void checkCommonsUpdateProfileAssertions(UpdateProfile dto, ProfileEntity profileEntity) {
        Assert.assertEquals(dto.getName(), profileEntity.getName());
        Assert.assertEquals(dto.getDescription(), profileEntity.getDescription());
        Assert.assertEquals(dto.getPecAddress(), profileEntity.getPecAddress());
        Assert.assertNotNull(profileEntity.getReferent());
        Assert.assertNotNull(dto.getReferent());
        Assert.assertEquals(dto.getReferent().getFirstName(), profileEntity.getReferent().getFirstName());
        Assert.assertEquals(dto.getReferent().getLastName(), profileEntity.getReferent().getLastName());
        Assert.assertEquals(dto.getReferent().getEmailAddress(), profileEntity.getReferent().getEmailAddress());
        Assert.assertEquals(dto.getReferent().getTelephoneNumber(), profileEntity.getReferent().getTelephoneNumber());

    }

}
