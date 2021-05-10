package it.gov.pagopa.cgn.portal.email;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Builder
@ToString
public class HelpRequestParams {

    private String helpCategory;
    private Optional<String> topic;
    private String message;
    private String referentFirstName;
    private String referentLastName;
    private String merchantLegalName;
    private String replyToEmailAddress;
}
