DROP MATERIALIZED VIEW IF EXISTS published_product_category;

CREATE MATERIALIZED VIEW published_product_category AS
SELECT DISTINCT pc.product_category,
                (SELECT COUNT(*)::int
                 FROM discount d2
                          JOIN discount_product_category pc2 ON (d2.discount_k = pc2.discount_fk)
                 WHERE pc2.product_category = pc.product_category
                   AND d2.state = 'PUBLISHED'
                   AND d2.start_date >= NOW() - INTERVAL '15 days') AS new_discounts
FROM discount d
         JOIN discount_product_category pc ON (d.discount_k = pc.discount_fk)
WHERE d.state = 'PUBLISHED'
ORDER BY new_discounts DESC, product_category ASC;

CREATE UNIQUE INDEX product_category_unique_idx ON published_product_category (product_category);
