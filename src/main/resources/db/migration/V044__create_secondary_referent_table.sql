CREATE TABLE secondary_referent
(
    referent_k     BIGSERIAL        NOT NULL,
    first_name       VARCHAR(50)  NOT NULL,
    last_name        VARCHAR(50)  NOT NULL,
    email_address    VARCHAR(320) NOT NULL,
    telephone_number VARCHAR(15)  NOT NULL,
    role             VARCHAR(100) NOT NULL DEFAULT 'N/A',
    profile_fk        BIGINT           NOT NULL,
	insert_time      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMPTZ,

    CONSTRAINT secondary_referent_pk PRIMARY KEY (referent_k),
    CONSTRAINT secondary_referent_profile_fk FOREIGN KEY (profile_fk) REFERENCES profile (profile_k)
);
CREATE INDEX secondary_referent_profile_fk_idx ON secondary_referent (profile_fk);