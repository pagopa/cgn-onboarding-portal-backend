package it.gov.pagopa.cgn.portal.enums;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


@Getter
@AllArgsConstructor
public enum ApiTokenTypeEnum {

    PRIMARY("primary"),
    SECONDARY("secondary");

    private final String code;


    public static ApiTokenTypeEnum fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            throw new InvalidRequestException("Invalid token type parameter: " + value);
        }
        return Arrays.stream(ApiTokenTypeEnum.values())
                .filter(apiTokenTypeEnum -> apiTokenTypeEnum.getCode().equals(value)).findFirst()
                .orElseThrow(()-> new InvalidRequestException("Invalid token type parameter: " + value));
    }

}
