package it.gov.pagopa.cgn.portal.enums;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
public class EnumTest {

    @Test
    public void BucketCodeExpiringThresholdEnum_Ok() {
        var sortedValues = Arrays.stream(BucketCodeExpiringThresholdEnum.values()).sorted().map(BucketCodeExpiringThresholdEnum::getValue).toArray(Integer[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new Integer[]{0, 10, 25, 50}, sortedValues);
    }


}
