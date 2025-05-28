ALTER TABLE discount_bucket_code_summary
    ADD COLUMN total_codes BIGINT NOT NULL DEFAULT 0;

UPDATE discount_bucket_code_summary s
SET total_codes = (SELECT number_of_codes
                       FROM bucket_code_load
                       WHERE discount_id = s.discount_fk
                       ORDER BY insert_time DESC LIMIT 1);