package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CheckProfileValidatorTest {

    private final CheckProfileValidator checkProfileValidator = new CheckProfileValidator();

    @Test
    public void Test_isValid_NoAddresses_FlagTrue_NoName_Valid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();

        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileEntity.setAddressList(null);
        profileEntity.setAllNationalAddresses(true);

        profileEntity.setName(null);
        profileEntity.setNameEn(null);
        profileEntity.setNameDe(null);

        Assertions.assertTrue(checkProfileValidator.isValid(profileEntity, null));
    }

    @Test
    public void Test_isValid_NoAddresses_FlagTrue_PartialNames_NotValid() {
        ProfileEntity profileEntity = TestUtils.createSampleProfileWithCommonFields();

        profileEntity.setSalesChannel(SalesChannelEnum.OFFLINE);
        profileEntity.setAddressList(null);
        profileEntity.setAllNationalAddresses(true);

        profileEntity.setName(null);
        profileEntity.setNameEn("null");
        profileEntity.setNameDe(null);

        Assertions.assertFalse(checkProfileValidator.isValid(profileEntity, null));
    }
}
