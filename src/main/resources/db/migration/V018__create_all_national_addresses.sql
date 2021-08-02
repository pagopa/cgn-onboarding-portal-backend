ALTER TABLE profile ADD COLUMN all_national_addresses BOOLEAN NULL;

ALTER TABLE address ALTER COLUMN latitude SET NULL;
ALTER TABLE address ALTER COLUMN longitude SET NULL;