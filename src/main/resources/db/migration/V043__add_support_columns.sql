ALTER TABLE profile
    ADD COLUMN support_type VARCHAR(20) NOT NULL DEFAULT 'PHONENUMBER';
ALTER TABLE profile
    ADD COLUMN support_value VARCHAR(500) NOT NULL DEFAULT '000000000';

DROP TYPE IF EXISTS support_type_enum CASCADE;

CREATE TYPE support_type_enum AS ENUM ('EMAILADDRESS', 'PHONENUMBER','WEBSITE');
CREATE CAST (character varying AS support_type_enum) WITH INOUT AS ASSIGNMENT;

ALTER TABLE profile
ALTER
COLUMN support_type TYPE support_type_enum
        using support_type::support_type_enum



