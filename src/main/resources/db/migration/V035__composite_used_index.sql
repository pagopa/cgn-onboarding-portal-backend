DROP INDEX discount_bucket_code_used_idx;
CREATE INDEX discount_bucket_code_used_idx ON public.discount_bucket_code USING btree (discount_fk ASC, used ASC)