package it.gov.pagopa.cgn.portal.enums;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Comparator;

@RunWith(SpringRunner.class)
public class EnumTest {

    @Test
    public void BucketCodeExpiringThresholdEnum_Ok() {
        var sortedValues = Arrays.stream(BucketCodeExpiringThresholdEnum.values())
                .sorted(Comparator.comparingInt(BucketCodeExpiringThresholdEnum::getValue))
                .map(BucketCodeExpiringThresholdEnum::getValue)
                .toArray(Integer[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new Integer[]{0, 10, 25, 50}, sortedValues);
    }

    @Test
    public void AgreementStateEnum_Ok() {
        var sortedValues = Arrays.stream(AgreementStateEnum.values())
                .sorted(Comparator.comparing(AgreementStateEnum::getCode))
                .map(AgreementStateEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"APPROVED", "DRAFT", "PENDING", "REJECTED"}, sortedValues);
    }


}
