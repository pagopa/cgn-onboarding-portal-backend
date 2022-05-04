package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CheckAddressesValidatorTest {

    private final CheckAddressesValidator checkAddressesValidator = new CheckAddressesValidator();

    @Test
    public void Test_isValid_OFFLINE_WithAddresses_FalseFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_OFFLINE_WithAddresses_TrueFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(true);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_OFFLINE_WithoutAddresses_FalseFlag_NotValid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);

        Assertions.assertFalse(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_OFFLINE_WithoutAddresses_TrueFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(true);
        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_BOTH_WithAddresses_FalseFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_BOTH_WithAddresses_TrueFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(true);
        profileEntity.setAddressList(TestUtils.createSampleAddress(profileEntity));
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_BOTH_WithoutAddresses_FalseFlag_NotValid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(false);
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);

        Assertions.assertFalse(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_BOTH_WithoutAddresses_TrueFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setAllNationalAddresses(true);
        profileEntity.setSalesChannel(SalesChannelEnum.BOTH);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_ONLINE_WithoutAddresses_FalseFlag_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();
        profileEntity.setSalesChannel(SalesChannelEnum.ONLINE);

        Assertions.assertTrue(checkAddressesValidator.isValid(profileEntity, null));
    }
}
