CREATE TYPE document_type_enum AS ENUM ('AGREEMENT', 'MANIFESTATION_OF_INTEREST');
CREATE CAST (character varying AS document_type_enum) WITH INOUT AS ASSIGNMENT;

CREATE TABLE document
(
    agreement_fk   VARCHAR(36)        NOT NULL,
    document_type document_type_enum NOT NULL,
    document_url  VARCHAR(255)       NOT NULL,
    insert_time   TIMESTAMPTZ        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMPTZ,


    CONSTRAINT document_pk PRIMARY KEY (document_url),
    CONSTRAINT document_agreement_doc_type_uk UNIQUE (agreement_fk, document_type),
    CONSTRAINT document_agreement_fk FOREIGN KEY (agreement_fk) REFERENCES agreement (agreement_k)
);
CREATE INDEX document_agreement_k_idx ON document (agreement_fk);
