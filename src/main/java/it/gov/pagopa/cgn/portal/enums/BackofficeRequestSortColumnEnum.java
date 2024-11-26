package it.gov.pagopa.cgn.portal.enums;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum BackofficeRequestSortColumnEnum {

    OPERATOR("Operator"), REQUEST_DATE("RequestDate"), STATE("State"), ASSIGNEE("Assignee");

    private final String value;

    public static BackofficeRequestSortColumnEnum fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(BackofficeRequestSortColumnEnum.values())
                .filter(columnEnum -> columnEnum.getValue().equals(value)).findFirst()
                .orElseThrow(()-> new InvalidRequestException("Sorting column not valid"));
    }
}
