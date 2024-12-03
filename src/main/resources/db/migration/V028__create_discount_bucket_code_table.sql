CREATE SEQUENCE "discount_bucket_code_k_seq"
INCREMENT BY 1 MINVALUE 1 NO MAXVALUE
START WITH 1 NO CYCLE;

CREATE TABLE discount_bucket_code
(
    bucket_code_k    BIGSERIAL    NOT NULL,
    discount_fk      BIGINT       NOT NULL,
    code             VARCHAR(20)  NOT NULL,
    used             BOOLEAN      DEFAULT FALSE NOT NULL,

    CONSTRAINT discount_bucket_code_pk PRIMARY KEY (bucket_code_k),
    CONSTRAINT discount_bucket_code_fk FOREIGN KEY (discount_fk) REFERENCES DISCOUNT (discount_k)
);