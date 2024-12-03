DROP MATERIALIZED VIEW IF EXISTS offline_merchant;
DROP MATERIALIZED VIEW IF EXISTS online_merchant;

ALTER TABLE discount_product_category
    ALTER COLUMN product_category TYPE VARCHAR(255);

WITH T3 AS (
    SELECT discount_fk, MIN(product_category) AS product_category
    FROM discount_product_category
    WHERE discount_fk in
          (
              SELECT T1.discount_fk
              FROM (
                       SELECT discount_fk, COUNT(*) AS C
                       FROM discount_product_category
                       WHERE product_category IN ('ENTERTAINMENT', 'FOOD_DRINK', 'HOTELS', 'SHOPPING')
                       GROUP BY discount_fk
                   ) T1
              WHERE T1.C > 1
          )
      AND product_category IN ('ENTERTAINMENT', 'FOOD_DRINK', 'HOTELS', 'SHOPPING')
    GROUP BY discount_fk
),
     T4 AS (
         SELECT D.discount_fk, D.product_category
         FROM T3
                  INNER JOIN discount_product_category D ON (D.discount_fk = T3.discount_fk)
         WHERE D.product_category != T3.product_category
           AND D.product_category IN ('ENTERTAINMENT', 'FOOD_DRINK', 'HOTELS', 'SHOPPING')
     )
DELETE
FROM discount_product_category D
WHERE (D.discount_fk, D.product_category) IN (SELECT * FROM T4);

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
