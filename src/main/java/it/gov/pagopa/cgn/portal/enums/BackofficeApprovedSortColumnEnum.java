package it.gov.pagopa.cgn.portal.enums;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum BackofficeApprovedSortColumnEnum {

    OPERATOR("Operator"), AGREEMENT_DATE("AgreementDate"), LAST_MODIFY_DATE("LastModifyDate"), PUBLISHED_DISCOUNTS("PublishedDiscounts");
    private final String value;

    public static BackofficeApprovedSortColumnEnum fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(BackofficeApprovedSortColumnEnum.values())
                .filter(columnEnum -> columnEnum.getValue().equals(value)).findFirst()
                .orElseThrow(() -> new InvalidRequestException("Sorting column not valid"));
    }
}
