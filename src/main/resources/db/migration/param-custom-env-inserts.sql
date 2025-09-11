--PROD

INSERT INTO param (param_group, param_key, param_value)
VALUES
('SEND_DISCOUNTS_EYCA_JOB', 'eyca.job.mailto', 'cgn-email-diagnostic@pagopa.it'),
('SEND_DISCOUNTS_EYCA_JOB', 'eyca.admin.mailto', 'pablo.guillerna@eyca.org;giuliana.vargetto@eyca.org');

--UAT

INSERT INTO param (param_group, param_key, param_value)
VALUES
('SEND_DISCOUNTS_EYCA_JOB', 'eyca.job.mailto', 'cgn-email-diagnostic@pagopa.it'),
('SEND_DISCOUNTS_EYCA_JOB', 'eyca.admin.mailto', 'alessandro.forcuti@dgsspa.com');
