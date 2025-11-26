package it.gov.pagopa.cgn.portal.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ParamGroupEnum {
    SEND_DISCOUNTS_EYCA_JOB,
    SEND_WEEKLY_SUMMARY_JOB,
    CHECK_AVAILABLE_DISC_JOB,
    SUSPEND_DISCOUNTS_JOB,
    CHECK_EXPIRING_DISC_JOB,
    SEND_LOW_DISC_BUCKET_CODES_NOTIF_JOB,
    CGN_JOB_FLAGS,
    CLEAN_DISCOUNTS_BUCKET_CODES_JOB;

    private final String code;

    ParamGroupEnum() {
        this.code = this.name();
    }
}