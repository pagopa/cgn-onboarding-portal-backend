DROP
MATERIALIZED VIEW IF EXISTS merchant;

CREATE
MATERIALIZED VIEW merchant AS
WITH merchants AS (
        SELECT a.agreement_k,
            COALESCE(NULLIF(p.name, ''), p.full_name) AS name,
            COALESCE(NULLIF(p.name_en, ''), p.full_name) AS name_en,
            p.description AS description,
            COALESCE(NULLIF(p.description_en, '-'), p.description) AS description_en
        FROM agreement a
            JOIN profile p ON (p.agreement_fk = a.agreement_k)
            JOIN discount d ON (d.agreement_fk = a.agreement_k)
        WHERE a.state = 'APPROVED'
            AND a.start_date <= CURRENT_DATE
            AND d.state = 'PUBLISHED'
            AND d.start_date <= CURRENT_DATE
            AND d.end_date >= CURRENT_DATE
    ),
     agreements_with_new_discounts AS (
        SELECT d.agreement_fk
        FROM discount d
        WHERE d.state = 'PUBLISHED'
            AND d.start_date <= CURRENT_DATE
            AND d.start_date >= CURRENT_DATE - INTERVAL '15 days'
            AND d.end_date >= CURRENT_DATE
        GROUP BY d.agreement_fk
    )
SELECT m.agreement_k           AS id,
       m.name,
       m.name_en,
       m.description,
       m.description_en,
       lower(m.name)           AS searchable_name,
       lower(m.name_en)        AS searchable_name_en,
       lower(m.description)    AS searchable_description,
       lower(m.description_en) AS searchable_description_en,
       CASE
           WHEN awnd.agreement_fk IS NOT NULL
               THEN TRUE
           ELSE FALSE
           END                 AS new_discounts,
       now()                   AS last_update
FROM merchants m
         LEFT JOIN agreements_with_new_discounts awnd ON awnd.agreement_fk = m.agreement_k
GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
ORDER BY new_discounts DESC, name ASC;

CREATE UNIQUE INDEX merchant_id_unique_idx ON merchant (id);

CREATE INDEX IF NOT EXISTS idx_merchant_search_name ON merchant USING gin (searchable_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_merchant_search_name_en ON merchant USING gin (searchable_name_en gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_merchant_search_desc ON merchant USING gin (searchable_description gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_merchant_search_desc_en ON merchant USING gin (searchable_description_en gin_trgm_ops);
