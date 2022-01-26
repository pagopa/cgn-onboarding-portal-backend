DROP MATERIALIZED VIEW IF EXISTS offline_merchant;

CREATE MATERIALIZED VIEW offline_merchant AS
WITH merchant AS (
    SELECT a.agreement_k,
           COALESCE(NULLIF(p.name, ''), p.full_name) AS name
    FROM agreement a
             JOIN profile p ON (p.agreement_fk = a.agreement_k)
    WHERE a.state = 'APPROVED'
      AND a.start_date <= CURRENT_TIMESTAMP
      AND CURRENT_TIMESTAMP <= a.end_date
      AND p.sales_channel IN ('OFFLINE', 'BOTH')
),
     product_categories AS (
         SELECT DISTINCT d.agreement_fk,
                         pc.product_category
         FROM discount d
                  JOIN discount_product_category pc ON (d.discount_k = pc.discount_fk)
         WHERE d.state = 'PUBLISHED'
           AND d.start_date <= CURRENT_TIMESTAMP
           AND CURRENT_TIMESTAMP <= d.end_date
           AND EXISTS(SELECT 1 FROM merchant m WHERE m.agreement_k = d.agreement_fk)
     ),
     merchant_with_categories AS (
         SELECT m.agreement_k,
                m.name,
                pc.product_category,
                CASE
                    WHEN pc.product_category = 'BANKING_SERVICES' THEN TRUE
                    ELSE FALSE
                    END AS banking_services,
                CASE
                    WHEN pc.product_category = 'CULTURE_AND_ENTERTAINMENT' THEN TRUE
                    ELSE FALSE
                    END AS culture_and_entertainment,
                CASE
                    WHEN pc.product_category = 'HEALTH' THEN TRUE
                    ELSE FALSE
                    END AS health,
                CASE
                    WHEN pc.product_category = 'HOME' THEN TRUE
                    ELSE FALSE
                    END AS home,
                CASE
                    WHEN pc.product_category = 'JOB_OFFERS' THEN TRUE
                    ELSE FALSE
                    END AS job_offers,
                CASE
                    WHEN pc.product_category = 'LEARNING' THEN TRUE
                    ELSE FALSE
                    END AS learning,
                CASE
                    WHEN pc.product_category = 'SPORTS' THEN TRUE
                    ELSE FALSE
                    END AS sports,
                CASE
                    WHEN pc.product_category = 'SUSTAINABLE_MOBILITY' THEN TRUE
                    ELSE FALSE
                    END AS sustainable_mobility,
                CASE
                    WHEN pc.product_category = 'TELEPHONY_AND_INTERNET' THEN TRUE
                    ELSE FALSE
                    END AS telephony_and_internet,
                CASE
                    WHEN pc.product_category = 'TRAVELLING' THEN TRUE
                    ELSE FALSE
                    END AS travelling
         FROM merchant m
                  JOIN product_categories pc
                       ON (m.agreement_k = pc.agreement_fk)
     ),
     merchant_without_address AS (
         SELECT m.agreement_k                        AS id,
                m.name,
                array_agg(m.product_category)        AS product_categories,
                lower(m.name)                        AS searchable_name,
                bool_or(m.banking_services)          AS banking_services,
                bool_or(m.culture_and_entertainment) AS culture_and_entertainment,
                bool_or(m.health)                    AS health,
                bool_or(m.home)                      AS home,
                bool_or(m.job_offers)                AS job_offers,
                bool_or(m.learning)                  AS learning,
                bool_or(m.sports)                    AS sports,
                bool_or(m.sustainable_mobility)      AS sustainable_mobility,
                bool_or(m.telephony_and_internet)    AS telephony_and_internet,
                bool_or(m.travelling)                AS travelling
         FROM merchant_with_categories m
         GROUP BY 1, 2
     )
SELECT m.id,
       m.name,
       m.product_categories,
       m.searchable_name,
       m.banking_services,
       m.culture_and_entertainment,
       m.health,
       m.home,
       m.job_offers,
       m.learning,
       m.sports,
       m.sustainable_mobility,
       m.telephony_and_internet,
       m.travelling,
       CASE
           WHEN a.full_address IS NULL AND p.all_national_addresses
               THEN 'Tutti i punti vendita sul territorio nazionale'
           ELSE a.full_address
           END     as full_address,
       a.latitude,
       a.longitude,
       a.address_k AS address_id,
       now()       AS last_update
FROM merchant_without_address m
         JOIN profile p on m.id = p.agreement_fk
         LEFT JOIN address a ON p.profile_k = a.profile_fk;

CREATE UNIQUE INDEX offline_merchant_id_unique_idx ON offline_merchant (id, address_id);

CREATE INDEX IF NOT EXISTS idx_offline_merchant_search_name ON offline_merchant USING gin (searchable_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_offline_merchant_lat_lon on offline_merchant (latitude, longitude);