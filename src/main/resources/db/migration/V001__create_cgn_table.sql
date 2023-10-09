CREATE TABLE agreement_user
(
    agreement_user_k VARCHAR(16) NOT NULL,
    agreement_id     VARCHAR(36) NOT NULL,
    insert_time      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMPTZ,

    CONSTRAINT agreement_user_pk PRIMARY KEY (agreement_user_k),
    CONSTRAINT agreement_user_agreement_id_uk UNIQUE (agreement_id)
);


CREATE TYPE agreement_state_enum AS ENUM ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED');
CREATE CAST (character varying AS agreement_state_enum) WITH INOUT AS ASSIGNMENT;

CREATE TABLE agreement
(
    agreement_k             VARCHAR(36)          NOT NULL,
    state                   agreement_state_enum NOT NULL,
    start_date              DATE,
    end_date                DATE,
    profile_modified_date   DATE,
    discounts_modified_date DATE,
    documents_modified_date DATE,
    insert_time             TIMESTAMPTZ          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMPTZ,

    CONSTRAINT agreement_pk PRIMARY KEY (agreement_k),
    CONSTRAINT agreement_fk FOREIGN KEY (agreement_k) REFERENCES agreement_user (agreement_id)
);


CREATE TABLE referent
(
    referent_k       BIGSERIAL    NOT NULL,
    first_name       VARCHAR(50)  NOT NULL,
    last_name        VARCHAR(50)  NOT NULL,
    email_address    VARCHAR(320) NOT NULL,
    telephone_number VARCHAR(15)  NOT NULL,
    insert_time      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMPTZ,

    CONSTRAINT referent_pk PRIMARY KEY (referent_k)
);


CREATE TYPE sales_channel_enum AS ENUM ('ONLINE', 'OFFLINE', 'BOTH');
CREATE CAST (character varying AS sales_channel_enum) WITH INOUT AS ASSIGNMENT;

CREATE TABLE profile
(
    profile_k     BIGSERIAL          NOT NULL,
    full_name     VARCHAR(100)       NOT NULL,
    name          VARCHAR(100),
    pec_address   VARCHAR(320)       NOT NULL,
    description   VARCHAR(300)       NOT NULL,
    sales_channel sales_channel_enum NOT NULL,
    website_url   VARCHAR(500),
    insert_time   TIMESTAMPTZ        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMPTZ,
    agreement_fk  VARCHAR(36)        NOT NULL,
    referent_fk   BIGINT             NOT NULL,

    CONSTRAINT profile_pk PRIMARY KEY (profile_k),
    CONSTRAINT profile_agreement_uk UNIQUE (agreement_fk),
    CONSTRAINT profile_referent_uk UNIQUE (referent_fk),
    CONSTRAINT profile_agreement_fk FOREIGN KEY (agreement_fk) REFERENCES agreement (agreement_k),
    CONSTRAINT profile_referent_fk FOREIGN KEY (referent_fk) REFERENCES referent (referent_k)
);


CREATE TABLE address
(
    address_k   BIGSERIAL    NOT NULL,
    street      VARCHAR(255) NOT NULL,
    zip_code    VARCHAR(5)   NOT NULL,
    city        VARCHAR(255) NOT NULL,
    district    VARCHAR(2)   NOT NULL,
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    profile_fk  BIGINT       NOT NULL,
    insert_time TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ,

    CONSTRAINT address_pk PRIMARY KEY (address_k),
    CONSTRAINT address_profile_fk FOREIGN KEY (profile_fk) REFERENCES profile (profile_k)
);
CREATE INDEX address_profile_fk_idx ON address (profile_fk);


CREATE TYPE discount_state_enum AS ENUM ('DRAFT', 'PUBLISHED', 'REJECTED');
CREATE CAST (character varying AS discount_state_enum) WITH INOUT AS ASSIGNMENT;

CREATE TABLE discount
(
    discount_k     BIGSERIAL           NOT NULL,
    state          discount_state_enum NOT NULL,
    name           VARCHAR(100)        NOT NULL,
    description    VARCHAR(250)        NOT NULL,
    start_date     DATE                NOT NULL,
    end_date       DATE                NOT NULL,
    discount_value DOUBLE PRECISION    NOT NULL CHECK (discount_value > 0 AND discount_value < 100),
    condition      VARCHAR(200)        NOT NULL,
    static_code    VARCHAR(100),
    agreement_fk   VARCHAR(36)         NOT NULL,
    eyca_update_id VARCHAR(24),
    insert_time    TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMPTZ,

    CONSTRAINT discount_pk PRIMARY KEY (discount_k),
    CONSTRAINT discount_agreement_fk FOREIGN KEY (agreement_fk) REFERENCES agreement (agreement_k)
);
CREATE INDEX discount_agreement_fk ON discount (agreement_fk);


CREATE TABLE discount_product_category
(
    product_category VARCHAR(100) NOT NULL,
    discount_fk      BIGINT       NOT NULL,
    insert_time      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMPTZ,

    CONSTRAINT discount_product_category_pk PRIMARY KEY (discount_fk, product_category),
    CONSTRAINT discount_product_category_fk FOREIGN KEY (discount_fk) REFERENCES DISCOUNT (discount_k)
);
