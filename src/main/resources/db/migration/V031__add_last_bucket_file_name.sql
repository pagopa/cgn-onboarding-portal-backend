ALTER TABLE discount
DROP
COLUMN last_bucket_code_file_uid;

ALTER TABLE discount
    ADD COLUMN last_bucket_code_load_id BIGINT;

ALTER TABLE discount
    ADD CONSTRAINT last_bucket_code_load_fk
        FOREIGN KEY (last_bucket_code_load_id)
            REFERENCES bucket_code_load (bucket_code_load_k)
            ON DELETE SET NULL;

ALTER TABLE bucket_code_load
    ADD COLUMN file_name VARCHAR(255);

UPDATE bucket_code_load
SET file_name = '-';

ALTER TABLE bucket_code_load
    ALTER COLUMN file_name SET NOT NULL;

ALTER TABLE discount_bucket_code
    ALTER COLUMN discount_fk DROP NOT NULL;

ALTER TABLE discount_bucket_code
DROP
CONSTRAINT discount_bucket_code_fk,
    ADD CONSTRAINT discount_bucket_code_fk
        FOREIGN KEY (discount_fk)
            REFERENCES DISCOUNT (discount_k)
            ON DELETE
SET NULL;
