ALTER TABLE address
    ADD COLUMN full_address VARCHAR(500);

UPDATE address
SET full_address = street || ', ' || zip_code || ' ' || city || ' ' || district;
COMMIT;

ALTER TABLE address
    ALTER COLUMN full_address SET NOT NULL;

ALTER TABLE address DROP COLUMN street;
ALTER TABLE address DROP COLUMN zip_code;
ALTER TABLE address DROP COLUMN city;
ALTER TABLE address DROP COLUMN district;

