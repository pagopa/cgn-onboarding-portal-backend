DROP VIEW IF EXISTS eyca_data_export;

CREATE VIEW eyca_data_export AS
SELECT row_number() over ()                                                               as "id",
       REPLACE(REPLACE(cat.categories::text, '{', ''), '}', '')                           as "categories",
       coalesce(p.full_name, p.name)                                                      as "vendor",
       ''                                                                                 as "name",
       d.name                                                                             as "name_local",
       ''                                                                                 as "text",
       d.description || ' - ' || d.condition || (
           CASE
               WHEN p.discount_code_type IS NULL
                   THEN 'Per accedere all''agevolazione, mostra la tua carta EYCA presso il punto vendita.'
               WHEN p.discount_code_type = 'STATIC'
                   THEN 'Per accedere all''agevolazione, usa il codice ' || d.static_code
               WHEN p.discount_code_type = 'LANDINGPAGE'
                   THEN 'Per accedere all''agevolazione, vai al link ' || d.landing_page_url
               END
           )                                                                              as "text_local",
       ''                                                                                 as "email",
       ''                                                                                 as "phone",
       p.website_url                                                                      as "web",
       ''                                                                                 as "tags",
       'https://cgnonboardingportal-p-cdnendpoint-storage.azureedge.net/' || ag.image_url as "image",
       CASE
           WHEN d.state = 'PUBLISHED' AND d.start_date <= CURRENT_DATE AND d.end_date >= CURRENT_DATE THEN 'Y'
           ELSE 'N' END                                                                   as "live",
       ''                                                                                 as "location_local_id",
       ad.full_address                                                                    as "street",
       ''                                                                                 as "city",
       ''                                                                                 as "zip",
       ''                                                                                 as "country",
       ''                                                                                 as "region",
       ''                                                                                 as "latitude",
       ''                                                                                 as "longitude"
FROM agreement ag
         INNER JOIN discount d ON d.agreement_fk = ag.agreement_k
         INNER JOIN profile p ON p.agreement_fk = ag.agreement_k
         INNER JOIN (
    select dpc.discount_fk,
           array_agg(eyca_map.eyca_cat) as categories
    from discount_product_category dpc
             INNER JOIN (
        SELECT *
        FROM (
                 VALUES ('BANKING_SERVICES', 'SV'),
                        ('CULTURE_AND_ENTERTAINMENT', 'GO'),
                        ('HEALTH', 'HB'),
                        ('HOME', 'SV'),
                        ('JOB_OFFERS', 'SV'),
                        ('LEARNING', 'LR'),
                        ('SPORTS', 'SP'),
                        ('SUSTAINABLE_MOBILITY', 'TT'),
                        ('TELEPHONY_AND_INTERNET', 'SV'),
                        ('TRAVELLING', 'TT')
             ) AS ec (cat, eyca_cat)
    ) eyca_map ON eyca_map.cat = dpc.product_category::text
    group by dpc.discount_fk
) as cat ON cat.discount_fk = d.discount_k
         LEFT JOIN address ad ON ad.profile_fk = p.profile_k
WHERE (d.visible_on_eyca = true
    AND ((p.sales_channel IN ('BOTH', 'OFFLINE') AND
          (p.discount_code_type IS NULL OR p.discount_code_type IN ('STATIC', 'LANDINGPAGE')))
        OR (p.sales_channel = 'ONLINE' AND (p.discount_code_type IN ('STATIC', 'LANDINGPAGE'))))
          );