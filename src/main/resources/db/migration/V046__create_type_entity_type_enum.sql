CREATE TYPE entity_type_enum AS ENUM ('PRIVATE', 'PUBLIC_ADMINISTRATION');
CREATE CAST (character varying AS entity_type_enum) WITH INOUT AS ASSIGNMENT;

ALTER TABLE agreement ADD COLUMN entity_type entity_type_enum;
ALTER TABLE profile ADD COLUMN entity_type entity_type_enum;
