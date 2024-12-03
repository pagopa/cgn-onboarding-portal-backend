package it.gov.pagopa.cgn.portal.enums;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum AssigneeEnum {

    ME("Me"), OTHERS("Others");

    private final String code;

    public static AssigneeEnum fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(AssigneeEnum.values())
                     .filter(assigneeEnum -> assigneeEnum.getCode().equals(value))
                     .findFirst()
                     .orElseThrow(() -> new InvalidRequestException("Assignee value not valid"));
    }
}
