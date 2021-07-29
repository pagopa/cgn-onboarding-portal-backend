ALTER TABLE discount_product_category ALTER COLUMN product_category TYPE VARCHAR(255);

DROP MATERIALIZED VIEW IF EXISTS offline_merchant;
DROP MATERIALIZED VIEW IF EXISTS online_merchant;

UPDATE discount_product_category SET product_category = 'ENTERTAINMENT' WHERE product_category = 'ENTERTAINMENTS';
UPDATE discount_product_category SET product_category = 'TRAVELLING' WHERE product_category IN ('TRAVELS', 'TRANSPORTATION');
UPDATE discount_product_category SET product_category = 'SERVICES' WHERE product_category = 'CONNECTIVITY';
UPDATE discount_product_category SET product_category = 'LEARNING' WHERE product_category IN ('BOOKS', 'ARTS');

DROP TYPE IF EXISTS product_category_enum CASCADE;

CREATE TYPE product_category_enum AS ENUM ('ENTERTAINMENT', 'TRAVELLING','FOOD_DRINK', 'SERVICES', 'LEARNING', 'HOTELS', 'SPORTS', 'HEALTH', 'SHOPPING');
CREATE CAST (character varying AS product_category_enum) WITH INOUT AS ASSIGNMENT;

ALTER TABLE discount_product_category ALTER COLUMN product_category TYPE product_category_enum
    using product_category::product_category_enum
