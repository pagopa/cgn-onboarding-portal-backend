DROP VIEW IF EXISTS backoffice_agreement;

CREATE VIEW backoffice_agreement AS
SELECT a.*,
       COUNT(d.*) as published_discounts
FROM agreement a
         LEFT JOIN discount d ON (d.agreement_fk = a.agreement_k and d.state = 'PUBLISHED')
GROUP BY a.agreement_k