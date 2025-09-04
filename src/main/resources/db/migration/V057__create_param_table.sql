CREATE SEQUENCE "param_k_seq"
INCREMENT BY 1 MINVALUE 1 NO MAXVALUE
START WITH 1 NO CYCLE;

CREATE TYPE param_group_enum AS ENUM ('SEND_WEEKLY_SUMMARY_JOB','SEND_LOW_DISC_BUCKET_CODES_NOTIF_JOB','CHECK_EXPIRING_DISC_JOB','SEND_DISCOUNTS_EYCA_JOB', 'WEEKLY_SUMMARY_JOB', 'CHECK_AVAILABLE_DISC_JOB', 'SUSPEND_DISCOUNTS_JOB');
CREATE CAST (character varying AS param_group_enum) WITH INOUT AS ASSIGNMENT;

CREATE TABLE param
(
    param_k      BIGSERIAL    NOT NULL,
    param_group  param_group_enum  NOT NULL,
    param_key    VARCHAR(100)  NOT NULL,
    param_value  VARCHAR      NOT NULL,
	insert_time  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMPTZ,

    CONSTRAINT param_pk PRIMARY KEY (param_k),
    CONSTRAINT param_group_key_unique UNIQUE (param_group, param_key)
);

CREATE INDEX param_pk_idx ON param (param_k);

INSERT INTO param (param_group, param_key, param_value)
VALUES
('CHECK_EXPIRING_DISC_JOB', 'check.expiring.discounts.job.cron', '0 30 23 * * ? *'),
('CHECK_EXPIRING_DISC_JOB', 'check.expiring.discounts.job.days', '15'),
('CHECK_AVAILABLE_DISC_JOB', 'check.available.discounts.bucket.codes.job.cron', '0 0/5 * * * ? *'),
('SEND_LOW_DISC_BUCKET_CODES_NOTIF_JOB', 'send.low.bucket.codes.notification.job.cron', '0 0/5 * * * ? *'),
('SUSPEND_DISCOUNTS_JOB', 'suspend.discounts.without.available.bucket.codes.job.cron', '0 0/5 * * * ? *'),
('SEND_WEEKLY_SUMMARY_JOB', 'send.weekly.discount.bucket.codes.summary.job.cron', '0 0 7 ? * WED'),
('SEND_DISCOUNTS_EYCA_JOB', 'send.discounts.to.eyca.job.cron', '0 0 2 * * ? *')
