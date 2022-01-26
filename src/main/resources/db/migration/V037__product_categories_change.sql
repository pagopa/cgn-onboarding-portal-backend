DROP MATERIALIZED VIEW IF EXISTS offline_merchant;
DROP MATERIALIZED VIEW IF EXISTS online_merchant;

ALTER TABLE discount_product_category
    ALTER COLUMN product_category TYPE VARCHAR(255);

UPDATE discount_product_category
SET product_category = 'CULTURE_AND_ENTERTAINMENT'
WHERE product_category IN ('ENTERTAINMENT', 'FOOD_DRINK', 'HOTELS', 'SHOPPING');

UPDATE discount_product_category
SET product_category = 'HOME'
WHERE product_category = 'SERVICES';

DROP TYPE IF EXISTS product_category_enum CASCADE;

CREATE TYPE product_category_enum AS ENUM (
    'BANKING_SERVICES',
    'CULTURE_AND_ENTERTAINMENT',
    'HEALTH',
    'HOME',
    'JOB_OFFERS',
    'LEARNING',
    'SPORTS',
    'SUSTAINABLE_MOBILITY',
    'TELEPHONY_AND_INTERNET',
    'TRAVELLING');
CREATE CAST (character varying AS product_category_enum) WITH INOUT AS ASSIGNMENT;

ALTER TABLE discount_product_category
    ALTER COLUMN product_category TYPE product_category_enum
        using product_category::product_category_enum
