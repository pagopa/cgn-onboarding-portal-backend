
CREATE OR REPLACE TYPE product_category_enum AS ENUM ('ENTERTAINMENT', 'TRAVELLING','FOOD_DRINK', 'SERVICES', 'LEARNING', 'HOTELS', 'SPORTS', 'HEALTH', 'SHOPPING');
CREATE CAST (character varying AS product_category_enum) WITH INOUT AS ASSIGNMENT;

DELETE FROM discount_product_category;

ALTER TABLE discount_product_category ALTER COLUMN product_category TYPE product_category_enum
    using product_category::product_category_enum
