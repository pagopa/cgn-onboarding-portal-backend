DROP
MATERIALIZED VIEW IF EXISTS online_merchant;

CREATE
MATERIALIZED VIEW online_merchant AS
WITH merchant AS (SELECT a.agreement_k,
                         COALESCE(NULLIF(p.name, ''), p.full_name) AS name,
                         p.website_url,
                         p.discount_code_type
                  FROM agreement a
                           JOIN profile p ON (p.agreement_fk = a.agreement_k)
                  WHERE a.state = 'APPROVED'
                    AND a.start_date <= CURRENT_DATE
                    AND p.sales_channel IN ('ONLINE', 'BOTH')),
     product_categories AS (SELECT DISTINCT d.agreement_fk,
                                            pc.product_category
                            FROM discount d
                                     JOIN discount_product_category pc ON (d.discount_k = pc.discount_fk)
                            WHERE d.state = 'PUBLISHED'
                              AND d.start_date <= CURRENT_DATE
                              AND CURRENT_DATE <= d.end_date
                              AND EXISTS(SELECT 1 FROM merchant m WHERE m.agreement_k = d.agreement_fk)),
     merchant_with_categories AS (SELECT m.agreement_k,
                                         m.name,
                                         m.website_url,
                                         m.discount_code_type,
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
                                           JOIN product_categories pc ON (m.agreement_k = pc.agreement_fk)),
     agreements_with_new_discounts AS (SELECT d.agreement_fk,
                                              array_agg(pc.product_category) as categories_with_new_discounts
                                       FROM discount d
                                                JOIN discount_product_category pc ON (d.discount_k = pc.discount_fk)
                                       WHERE d.state = 'PUBLISHED'
		                                 AND d.start_date <= CURRENT_DATE
                                         AND d.start_date >= CURRENT_DATE - INTERVAL '15 days'
                                         AND d.end_date >= CURRENT_DATE
                                       GROUP BY d.agreement_fk),
	new_discounts_count AS (
		SELECT d.agreement_fk,
	   count(*) as number_of_new_discounts
		FROM discount d
		WHERE d.state = 'PUBLISHED'
		 AND d.start_date <= CURRENT_DATE
		 AND d.start_date >= CURRENT_DATE - INTERVAL '15 days'
		 AND d.end_date >= CURRENT_DATE
		GROUP BY d.agreement_fk
	)
SELECT m.agreement_k                        AS id,
       m.name,
       m.website_url,
       m.discount_code_type,
       CASE
           WHEN a.agreement_fk IS NOT NULL
               THEN TRUE
           ELSE FALSE
           END                              AS new_discounts,
       a.categories_with_new_discounts,
       ndc.number_of_new_discounts,
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
       bool_or(m.travelling)                AS travelling,
       now()                                AS last_update
FROM merchant_with_categories m
         LEFT JOIN agreements_with_new_discounts a ON a.agreement_fk = m.agreement_k
         LEFT JOIN new_discounts_count ndc ON ndc.agreement_fk = m.agreement_k
GROUP BY 1, 2, 3, 4, 5, 6, 7
ORDER BY new_discounts DESC, name ASC;

CREATE UNIQUE INDEX online_merchant_id_unique_idx ON online_merchant (id);

CREATE INDEX IF NOT EXISTS idx_online_merchant_search_name ON online_merchant USING gin (searchable_name gin_trgm_ops);
