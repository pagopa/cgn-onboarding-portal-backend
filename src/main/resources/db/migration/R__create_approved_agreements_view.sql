DROP VIEW IF EXISTS approved_agreements;

CREATE VIEW approved_agreements AS
SELECT a.agreement_k,
       a.information_last_update_date,
       a.start_date,
       a.state,
       p.full_name,
       COUNT(d.*) as published_discounts
FROM agreement a
         JOIN profile p ON (a.agreement_k = p.agreement_fk)
         LEFT JOIN discount d ON (d.agreement_fk = a.agreement_k and d.state = 'PUBLISHED')
GROUP BY a.agreement_k, a.information_last_update_date, a.start_date, a.state, p.full_name
ORDER BY p.full_name, a.start_date