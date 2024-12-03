ALTER TABLE profile
    ADD COLUMN tax_code_or_vat VARCHAR(16) NOT NULL DEFAULT 'N/A';
ALTER TABLE profile
    ALTER COLUMN tax_code_or_vat DROP DEFAULT;
