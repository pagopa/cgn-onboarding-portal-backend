ALTER TABLE profile DROP COLUMN support_type;
ALTER TABLE profile DROP COLUMN support_value;
DROP TYPE IF EXISTS support_type_enum CASCADE;
