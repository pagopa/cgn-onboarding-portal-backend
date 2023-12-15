DROP VIEW IF EXISTS approved_agreements;

CREATE VIEW approved_agreements AS
WITH discounts_counter AS (SELECT a.agreement_k,
                                  COUNT(d.*) as published_discounts
                           FROM agreement a
                                    LEFT JOIN discount d
                                              ON (d.agreement_fk = a.agreement_k and d.state = 'PUBLISHED' and
                                                  CURRENT_DATE <= d.end_date)
                           GROUP BY a.agreement_k),
     test_pending_checker AS (SELECT a.agreement_k,
                                     COUNT(d.state) > 0 as test_pending
                              FROM agreement a
                                       LEFT JOIN discount d
                                                 ON (d.agreement_fk = a.agreement_k and d.state = 'TEST_PENDING' and
                                                     CURRENT_DATE <= d.end_date)
                              GROUP BY a.agreement_k)
SELECT a.agreement_k,
       a.information_last_update_date,
       a.start_date,
       a.state,
       a.assignee,
       a.request_approval_time,
       p.full_name,
       c.published_discounts,
       t.test_pending,
       a.entity_type
FROM agreement a
         JOIN profile p ON (a.agreement_k = p.agreement_fk)
         JOIN discounts_counter c ON (a.agreement_k = c.agreement_k)
         JOIN test_pending_checker t ON (a.agreement_k = t.agreement_k)