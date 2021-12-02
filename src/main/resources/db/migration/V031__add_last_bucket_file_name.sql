ALTER TABLE discount ADD COLUMN last_bucket_code_file_name VARCHAR(255);
ALTER TABLE bucket_code_load ADD COLUMN file_name VARCHAR(255);
UPDATE bucket_code_load SET file_name = 'n.d.';
ALTER TABLE bucket_code_load ALTER COLUMN file_name SET NOT NULL;
