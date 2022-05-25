ALTER TABLE discount
    ALTER COLUMN state TYPE VARCHAR(255);

DROP TYPE IF EXISTS discount_state_enum CASCADE;
CREATE TYPE discount_state_enum AS ENUM ('DRAFT', 'PUBLISHED', 'REJECTED', 'TO_TEST', 'TEST_OK', 'TEST_KO');
CREATE CAST (character varying AS discount_state_enum) WITH INOUT AS ASSIGNMENT;

ALTER TABLE discount
    ALTER COLUMN state TYPE discount_state_enum
        using state::discount_state_enum
