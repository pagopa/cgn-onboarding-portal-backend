CREATE TYPE entity_type_enum AS ENUM ('PRIVATE', 'PUBLIC_ADMINISTRATION');
CREATE CAST (character varying AS entity_type_enum) WITH INOUT AS ASSIGNMENT;