ALTER TABLE profile
    ADD COLUMN all_national_addresses BOOLEAN NULL;

ALTER TABLE address
    ALTER COLUMN latitude DROP NOT NULL;
ALTER TABLE address
    ALTER COLUMN longitude DROP NOT NULL;