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
    public void BucketCodeExpiringThresholdEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BucketCodeExpiringThresholdEnum.valueOf("NOT_AVAILABLE");
        });
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

    @Test
    public void AgreementStateEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            AgreementStateEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void ApiTokenTypeEnum_Ok() {
        var sortedValues = Arrays.stream(ApiTokenTypeEnum.values())
                .sorted(Comparator.comparing(ApiTokenTypeEnum::getCode))
                .map(ApiTokenTypeEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(2, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"Primary", "Secondary"}, sortedValues);
    }

    @Test
    public void ApiTokenTypeEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ApiTokenTypeEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void AssigneeEnum_Ok() {
        var sortedValues = Arrays.stream(AssigneeEnum.values())
                .sorted(Comparator.comparing(AssigneeEnum::getCode))
                .map(AssigneeEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(2, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"Me", "Others"}, sortedValues);
    }

    @Test
    public void AssigneeEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            AssigneeEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void BackofficeApprovedSortColumnEnum_Ok() {
        var sortedValues = Arrays.stream(BackofficeApprovedSortColumnEnum.values())
                .sorted(Comparator.comparing(BackofficeApprovedSortColumnEnum::getValue))
                .map(BackofficeApprovedSortColumnEnum::getValue)
                .toArray(String[]::new);
        Assertions.assertEquals(3, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"AgreementDate", "LastModifyDate", "Operator"}, sortedValues);
    }

    @Test
    public void BackofficeApprovedSortColumnEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BackofficeApprovedSortColumnEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void BackofficeRequestSortColumnEnum_Ok() {
        var sortedValues = Arrays.stream(BackofficeRequestSortColumnEnum.values())
                .sorted(Comparator.comparing(BackofficeRequestSortColumnEnum::getValue))
                .map(BackofficeRequestSortColumnEnum::getValue)
                .toArray(String[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"Assignee", "Operator", "RequestDate", "State"}, sortedValues);
    }

    @Test
    public void BackofficeRequestSortColumnEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BackofficeRequestSortColumnEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void BucketCodeLoadStatusEnum_Ok() {
        var sortedValues = Arrays.stream(BucketCodeLoadStatusEnum.values())
                .sorted(Comparator.comparing(BucketCodeLoadStatusEnum::getCode))
                .map(BucketCodeLoadStatusEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"FAILED", "FINISHED", "PENDING", "RUNNING"}, sortedValues);
    }

    @Test
    public void BucketCodeLoadStatusEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BucketCodeLoadStatusEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void DiscountCodeTypeEnum_Ok() {
        var sortedValues = Arrays.stream(DiscountCodeTypeEnum.values())
                .sorted(Comparator.comparing(DiscountCodeTypeEnum::getCode))
                .map(DiscountCodeTypeEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"API", "BUCKET", "LANDINGPAGE", "STATIC"}, sortedValues);
    }

    @Test
    public void DiscountCodeTypeEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DiscountCodeTypeEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void DiscountStateEnum_Ok() {
        var sortedValues = Arrays.stream(DiscountStateEnum.values())
                .sorted(Comparator.comparing(DiscountStateEnum::getCode))
                .map(DiscountStateEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(3, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"DRAFT", "PUBLISHED", "SUSPENDED"}, sortedValues);
    }

    @Test
    public void DiscountStateEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DiscountStateEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void DocumentTypeEnum_Ok() {
        var sortedValues = Arrays.stream(DocumentTypeEnum.values())
                .sorted(Comparator.comparing(DocumentTypeEnum::getCode))
                .map(DocumentTypeEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(4, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"ADHESION_REQUEST", "AGREEMENT", "BACKOFFICE_ADHESION_REQUEST", "BACKOFFICE_AGREEMENT"}, sortedValues);
    }

    @Test
    public void DocumentTypeEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DocumentTypeEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void ProductCategoryEnum_Ok() {
        var sortedValues = Arrays.stream(ProductCategoryEnum.values())
                .sorted(Comparator.comparing(ProductCategoryEnum::getDescription))
                .map(ProductCategoryEnum::getDescription)
                .toArray(String[]::new);
        Assertions.assertEquals(9, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"Hotel",
                "Istruzione e formazione",
                "Ristoranti e cucina",
                "Salute e benessere",
                "Servizi",
                "Shopping",
                "Sport",
                "Tempo libero",
                "Viaggi Trasporti e Mobilità"}, sortedValues);
    }

    @Test
    public void ProductCategoryEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ProductCategoryEnum.valueOf("NOT_AVAILABLE");
        });
    }

    @Test
    public void SalesChannelEnum_Ok() {
        var sortedValues = Arrays.stream(SalesChannelEnum.values())
                .sorted(Comparator.comparing(SalesChannelEnum::getCode))
                .map(SalesChannelEnum::getCode)
                .toArray(String[]::new);
        Assertions.assertEquals(3, sortedValues.length);
        Assertions.assertArrayEquals(new String[]{"BOTH", "OFFLINE", "ONLINE"}, sortedValues);
    }

    @Test
    public void SalesChannelEnum_Ko() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SalesChannelEnum.valueOf("NOT_AVAILABLE");
        });
    }
}
