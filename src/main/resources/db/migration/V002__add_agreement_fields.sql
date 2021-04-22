ALTER TABLE agreement ADD COLUMN first_discount_publishing_date DATE;
ALTER TABLE agreement ADD COLUMN reject_reason_msg VARCHAR(500);
ALTER TABLE agreement ADD COLUMN image_url VARCHAR(500);

ALTER TABLE agreement DROP COLUMN profile_modified_date;
ALTER TABLE agreement DROP COLUMN discounts_modified_date;
ALTER TABLE agreement DROP COLUMN documents_modified_date;


ALTER TABLE referent ADD COLUMN role VARCHAR(100) NOT NULL;

CREATE TYPE discount_code_type_enum AS ENUM ('STATIC', 'API');
CREATE CAST (character varying AS discount_code_type_enum) WITH INOUT AS ASSIGNMENT;

ALTER TABLE profile ADD COLUMN legal_office VARCHAR(200) NOT NULL;
ALTER TABLE profile ADD COLUMN telephone_number VARCHAR(15) NOT NULL;
ALTER TABLE profile ADD COLUMN legal_representative_full_name VARCHAR(200) NOT NULL;
ALTER TABLE profile ADD COLUMN legal_representative_tax_code VARCHAR(16) NOT NULL;
ALTER TABLE profile ADD COLUMN discount_code_type discount_code_type_enum;
