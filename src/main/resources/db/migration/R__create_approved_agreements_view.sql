DROP VIEW IF EXISTS approved_agreements;

CREATE VIEW approved_agreements AS
WITH discounts_counter AS (
    SELECT a.agreement_k,
           COUNT(d.*) as published_discounts
    FROM agreement a
             LEFT JOIN discount d ON (d.agreement_fk = a.agreement_k and d.state = 'PUBLISHED')
    GROUP BY a.agreement_k
)
SELECT a.agreement_k,
       a.information_last_update_date,
       a.start_date,
       a.state,
       a.assignee,
       a.request_approval_time,
       p.full_name,
       c.published_discounts
FROM agreement a
         JOIN profile p ON (a.agreement_k = p.agreement_fk)
         JOIN discounts_counter c ON (a.agreement_k = c.agreement_k)