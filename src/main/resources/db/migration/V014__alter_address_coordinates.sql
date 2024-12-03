UPDATE address
SET latitude  = 0,
    longitude = 0
where latitude is null
   or longitude is null;
COMMIT;

ALTER TABLE address
    ALTER COLUMN latitude SET NOT NULL;
ALTER TABLE address
    ALTER COLUMN longitude SET NOT NULL;
