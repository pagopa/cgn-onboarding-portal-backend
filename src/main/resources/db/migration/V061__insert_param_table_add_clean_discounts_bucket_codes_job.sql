INSERT INTO param (param_group, param_key, param_value)
VALUES
('CLEAN_DISCOUNTS_BUCKET_CODES_JOB', 'clean.discounts.bucket.codes.retention.period', 'P6M'),
('CLEAN_DISCOUNTS_BUCKET_CODES_JOB', 'clean.discounts.bucket.codes.job.cron', '0 0 3 * * ? *')