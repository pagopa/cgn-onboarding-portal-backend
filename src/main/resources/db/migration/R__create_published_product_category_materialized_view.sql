DROP MATERIALIZED VIEW IF EXISTS published_product_category;

CREATE MATERIALIZED VIEW published_product_category AS
SELECT DISTINCT pc.product_category
FROM discount d
         JOIN discount_product_category pc ON (d.discount_k = pc.discount_fk)
WHERE d.state = 'PUBLISHED';

CREATE UNIQUE INDEX product_category_unique_idx ON published_product_category (product_category);
