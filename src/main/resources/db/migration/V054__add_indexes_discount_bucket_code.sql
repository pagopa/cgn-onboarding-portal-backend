CREATE INDEX IF NOT EXISTS discount_bucket_discount_fk_idx ON discount_bucket_code (discount_fk);
CREATE INDEX IF NOT EXISTS discount_bucket_discount_fk_used_idx ON discount_bucket_code (discount_fk, used);
