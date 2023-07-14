CREATE TABLE secondaryreferent
(
    secondaryreferent_k     BIGSERIAL        NOT NULL,
    first_name       VARCHAR(50)  NOT NULL,
    last_name        VARCHAR(50)  NOT NULL,
    email_address    VARCHAR(320) NOT NULL,
    telephone_number VARCHAR(15)  NOT NULL,
    profile_fk        BIGINT           NOT NULL,
	insert_time      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMPTZ,

    CONSTRAINT secondaryreferent_pk PRIMARY KEY (secondaryreferent_k),
    CONSTRAINT secondaryreferent_profile_fk FOREIGN KEY (profile_fk) REFERENCES profile (profile_k)
);
CREATE INDEX secondaryreferent_profile_fk_idx ON secondaryreferent (profile_fk);