UPDATE profile
SET all_national_addresses = true
WHERE sales_channel IN ('BOTH', 'OFFLINE')
  AND profile_k NOT IN (SELECT profile_fk from address);
UPDATE profile
SET all_national_addresses = false
WHERE sales_channel IN ('BOTH', 'OFFLINE')
  AND profile_k IN (SELECT profile_fk from address);