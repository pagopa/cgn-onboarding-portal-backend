ALTER TABLE profile ALTER COLUMN pec_address TYPE VARCHAR(100);
ALTER TABLE profile ALTER COLUMN legal_office TYPE VARCHAR(255);

ALTER TABLE referent ALTER COLUMN first_name TYPE VARCHAR(100);
ALTER TABLE referent ALTER COLUMN last_name TYPE VARCHAR(100);
ALTER TABLE referent ALTER COLUMN email_address TYPE VARCHAR(100);

ALTER TABLE discount DROP CONSTRAINT discount_discount_value_check,
    ADD CONSTRAINT discount_discount_value_check CHECK (discount_value >= 5 AND discount_value < 100);

ALTER TABLE discount ALTER COLUMN suspended_reason_message TYPE VARCHAR(500);
