CREATE TYPE change_audit_subject_type_enum AS ENUM ('AGREEMENT', 'DISCOUNT', 'PROFILE');
CREATE CAST (character varying AS change_audit_subject_type_enum) WITH INOUT AS ASSIGNMENT;

CREATE TYPE change_audit_operation_type_enum AS ENUM ('INSERT', 'UPDATE', 'DELETE');
CREATE CAST (character varying AS change_audit_operation_type_enum) WITH INOUT AS ASSIGNMENT;

CREATE SEQUENCE change_audit_change_audit_k_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE change_audit
(
    change_audit_k    BIGINT                            NOT NULL DEFAULT nextval('change_audit_change_audit_k_seq'),
    subject_id        VARCHAR(100)                      NOT NULL,
    partner_full_name VARCHAR(100)                      NOT NULL,
    actor_ref         VARCHAR(200),
    subject_type      change_audit_subject_type_enum    NOT NULL,
    operation_type    change_audit_operation_type_enum  NOT NULL,
    insert_time       TIMESTAMPTZ                       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    value             JSONB                             NOT NULL,

    CONSTRAINT change_audit_pk PRIMARY KEY (change_audit_k)
);

ALTER SEQUENCE change_audit_change_audit_k_seq OWNED BY change_audit.change_audit_k;

CREATE INDEX change_audit_subject_id_idx ON change_audit (subject_id);
CREATE INDEX change_audit_insert_time_idx ON change_audit (insert_time);