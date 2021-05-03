DROP MATERIALIZED VIEW IF EXISTS online_merchant;

CREATE MATERIALIZED VIEW online_merchant AS
WITH merchant AS (
    SELECT a.agreement_k,
           p.name,
           p.website_url
    FROM agreement a
             JOIN profile p ON (p.agreement_fk = a.agreement_k)
    WHERE a.state = 'APPROVED'
      AND a.start_date <= CURRENT_TIMESTAMP
      AND CURRENT_TIMESTAMP <= a.end_date
      AND p.sales_channel IN ('ONLINE', 'BOTH')
),
     product_categories AS (
         SELECT DISTINCT d.agreement_fk,
                         pc.product_category
         FROM discount d
                  JOIN discount_product_category pc ON (d.discount_k = pc.discount_fk)
         WHERE d.state = 'PUBLISHED'
           AND d.start_date <= CURRENT_TIMESTAMP
           AND CURRENT_TIMESTAMP <= d.end_date
           AND d.agreement_fk IN (SELECT agreement_k FROM merchant)
     ),
     merchant_with_categories AS (
         SELECT m.agreement_k,
                m.name,
                m.website_url,
                pc.product_category,
                CASE
                    WHEN pc.product_category = 'ARTS' THEN TRUE
                    ELSE FALSE
                    END AS arts,
                CASE
                    WHEN pc.product_category = 'BOOKS' THEN TRUE
                    ELSE FALSE
                    END AS books,
                CASE
                    WHEN pc.product_category = 'CONNECTIVITY' THEN TRUE
                    ELSE FALSE
                    END AS connectivity,
                CASE
                    WHEN pc.product_category = 'ENTERTAINMENTS' THEN TRUE
                    ELSE FALSE
                    END AS entertainments,
                CASE
                    WHEN pc.product_category = 'HEALTH' THEN TRUE
                    ELSE FALSE
                    END AS health,
                CASE
                    WHEN pc.product_category = 'SPORTS' THEN TRUE
                    ELSE FALSE
                    END AS sports,
                CASE
                    WHEN pc.product_category = 'TRANSPORTATION' THEN TRUE
                    ELSE FALSE
                    END AS transportation,
                CASE
                    WHEN pc.product_category = 'TRAVELS' THEN TRUE
                    ELSE FALSE
                    END AS travels
         FROM merchant m
                  JOIN product_categories pc ON (m.agreement_k = pc.agreement_fk)
     )
SELECT m.agreement_k                 AS id,
       m.name,
       m.website_url,
       array_agg(m.product_category) AS product_categories,
       lower(m.name)                 AS searchable_name,
       bool_or(m.arts)               AS arts,
       bool_or(m.books)              AS books,
       bool_or(m.connectivity)       AS connectivity,
       bool_or(m.entertainments)     AS entertainments,
       bool_or(m.health)             AS health,
       bool_or(m.sports)             AS sports,
       bool_or(m.transportation)     AS transportation,
       bool_or(m.travels)            AS travels
FROM merchant_with_categories m
GROUP BY 1, 2, 3;

CREATE UNIQUE INDEX online_merchant_id_unique_idx ON online_merchant (id);
