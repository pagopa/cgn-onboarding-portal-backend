package it.gov.pagopa.cgn.portal.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TemplateEmail {

    NEW_AGREEMENT("email/agreement-request-new.html"),
    APPROVED_AGREEMENT_BOTH("email/agreement-request-approved-both.html"),
    APPROVED_AGREEMENT_OFFLINE("email/agreement-request-approved-offline.html"),
    APPROVED_AGREEMENT_ONLINE_API_CODE("email/agreement-request-approved-online-api-code.html"),
    APPROVED_AGREEMENT_ONLINE_STATIC_CODE("email/agreement-request-approved-online-static-code.html"),
    REJECTED_AGREEMENT("email/agreement-request-rejected.html"),
    SUSPENDED_DISCOUNT("email/discount-suspended.html"),
    EXPIRING_DISCOUNT("email/discount-expiring.html"),
    DISCOUNT_TEST_REQUEST("email/discount-test-request.html"),
    DISCOUNT_TEST_PASSED("email/discount-test-passed.html"),
    DISCOUNT_TEST_FAILED("email/discount-test-failed.html"),
    EXPIRING_BUCKET_CODES("email/bucket-codes-expiring.html"),
    WEEKLY_SUMMARY_BUCKET_CODES("email/bucket-codes-weekly-summary.html"),
    EXPIRED_BUCKET_CODES("email/bucket-codes-expired.html"),
    HELP_REQUEST("email/help-request.html");

    private final String templateName;
}
