package it.gov.pagopa.cgn.portal.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TemplateEmail {

    NEW_AGREEMENT("email/agreement-request-new.html"),
    APPROVED_AGREEMENT("email/agreement-request-approved-both.html"),
    REJECTED_AGREEMENT("email/agreement-request-rejected.html"),
    SUSPENDED_DISCOUNT("email/discount-suspended.html"),
    EXPIRED_DISCOUNT("email/discount-expiring.html"),
    HELP_REQUEST("email/help-request.html");

    private final String templateName;
}
