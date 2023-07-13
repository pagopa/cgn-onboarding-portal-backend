CREATE TABLE secondaryrecipient
(
    secondaryrecipient_k     BIGSERIAL        NOT NULL,
    email_address     VARCHAR(100)     NOT NULL,
    profile_fk        BIGINT           NOT NULL,

    CONSTRAINT secondaryrecipient_pk PRIMARY KEY (secondaryrecipient_k),
    CONSTRAINT secondaryrecipient_profile_fk FOREIGN KEY (profile_fk) REFERENCES profile (profile_k)
);
CREATE INDEX secondaryrecipient_profile_fk ON secondaryrecipient (profile_fk);