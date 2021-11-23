CREATE SEQUENCE "bucket_code_load_k_seq"
INCREMENT BY 1 MINVALUE 1 NO MAXVALUE
START WITH 1 NO CYCLE;

CREATE TABLE bucket_code_load
(
    bucket_code_load_k    BIGSERIAL    NOT NULL,
    discount_id           BIGINT       NOT NULL,
    status                VARCHAR(50)  NOT NULL,
    uid                   VARCHAR(255) NOT NULL,
    number_of_codes       BIGINT               ,
    insert_time             TIMESTAMPTZ          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMPTZ,

    CONSTRAINT bucket_code_load_pk PRIMARY KEY (bucket_code_load_k)
);

ALTER TABLE discount ADD COLUMN last_bucket_code_file_uid VARCHAR(255);

ALTER TABLE discount_bucket_code ADD COLUMN bucket_code_load_id BIGINT NOT NULL;