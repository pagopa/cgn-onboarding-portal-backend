package it.gov.pagopa.cgn.portal.recaptcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GoogleRecaptchaResponse {
    private boolean success;
}
