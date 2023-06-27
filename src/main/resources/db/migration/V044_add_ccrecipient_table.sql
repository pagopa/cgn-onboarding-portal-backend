CREATE TABLE ccrecipient
(
    ccrecipient_k     BIGSERIAL        NOT NULL,
    email_address     VARCHAR(100)     NOT NULL,
    profile_fk        BIGINT           NOT NULL,

    CONSTRAINT ccrecipient_pk PRIMARY KEY (ccrecipient_k),
    CONSTRAINT ccrecipient_profile_fk FOREIGN KEY (profile_fk) REFERENCES profile (profile_k)
);
CREATE INDEX ccrecipient_profile_fk ON ccrecipient (profile_fk);