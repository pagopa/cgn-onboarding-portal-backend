DROP VIEW IF EXISTS eyca_data_export;

CREATE VIEW eyca_data_export AS
SELECT
    distinct on (d.discount_k) "discount_id",						
    row_number() over () as "id",					
	d.state as "state",
    REPLACE(REPLACE(cat.categories :: text, '{', ''), '}', '') as "categories",
    p.profile_k as "profile_id",
	coalesce(p.name, p.full_name) as "vendor",
    d.eyca_update_id AS "eyca_update_id",
    d.name_en as "name",
    d.start_date as "start_date",
    d.end_date as "end_date",
    d.name as "name_local",
    CONCAT_WS(
        ' - ',
        NULLIF(TRIM(d.description_en), ''),
        NULLIF(TRIM(d.condition_en), ''),
        (
            CASE
                WHEN p.discount_code_type IS NULL THEN 'To access the discount, show your EYCA card at the point of sale.'
                WHEN p.discount_code_type = 'STATIC' THEN 'To access the discount, use the code ' || d.static_code
                WHEN p.discount_code_type = 'LANDINGPAGE' THEN 'To access the discount, use the link ' || d.landing_page_url
            END
        )
    ) as "text",
    CONCAT_WS(
        ' - ',
        NULLIF(TRIM(d.description), ''),
        NULLIF(TRIM(d.condition), ''),
        (
            CASE
                WHEN p.discount_code_type IS NULL THEN 'Per accedere all''agevolazione, mostra la tua carta EYCA presso il punto vendita.'
                WHEN p.discount_code_type = 'STATIC' THEN 'Per accedere all''agevolazione, usa il codice ' || d.static_code
                WHEN p.discount_code_type = 'LANDINGPAGE' THEN 'Per accedere all''agevolazione, vai al link ' || d.landing_page_url
            END
        )
    ) as "text_local",
    '' as "email",
    '' as "phone",
    p.website_url as "web",
    '' as "tags",
    'https://cgnonboardingportal-p-cdnendpoint-storage.azureedge.net/' || ag.image_url as "image",

	L.live as "live",

    '' as "location_local_id",
    ad.full_address as "street",
    '' as "city",
    '' as "zip",
    '' as "country",
    '' as "region",
    '' as "latitude",
    '' as "longitude",
	p.sales_channel as "sales_channel",
    (
        CASE
            WHEN p.discount_code_type IS NULL THEN 'SHOP'
            WHEN p.discount_code_type = 'STATIC' THEN 'STATIC CODE'
            WHEN p.discount_code_type = 'LANDINGPAGE' THEN 'LANDING PAGE'
            WHEN p.discount_code_type = 'BUCKET' THEN 'LIST OF STATIC CODES'
        END
    ) AS "discount_type",
	d.landing_page_referrer as "landing_page_referrer",
    p.referent_fk as "referent"
FROM
	(
		SELECT CASE
			WHEN state = 'PUBLISHED'
			AND start_date <= CURRENT_DATE
			AND end_date >= CURRENT_DATE THEN 'Y'
		ELSE 'N'
		END as "live",
		sd.discount_k AS "discount_id"
		FROM discount sd
	) as L,	

    agreement ag
    INNER JOIN discount d ON d.agreement_fk = ag.agreement_k
    INNER JOIN profile p ON p.agreement_fk = ag.agreement_k
    INNER JOIN (
        select
            dpc.discount_fk,
            array_agg(eyca_map.eyca_cat) as categories
        from
            discount_product_category dpc
            INNER JOIN (
                SELECT
                    *
                FROM
                    (
                        VALUES
                            ('BANKING_SERVICES', 'SV'),
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
            ) eyca_map ON eyca_map.cat = dpc.product_category :: text
        group by
            dpc.discount_fk
    ) as cat ON cat.discount_fk = d.discount_k
    LEFT JOIN address ad ON ad.profile_fk = p.profile_k
WHERE
d.visible_on_eyca = true
AND L.live = 'Y' 
AND L.discount_id = d.discount_k
AND (
    (p.sales_channel = 'OFFLINE' AND p.discount_code_type IS NULL)
    OR
    (p.sales_channel IN ('ONLINE', 'BOTH') AND p.discount_code_type IN ('STATIC', 'BUCKET'))
    OR
    (p.sales_channel IN ('ONLINE', 'BOTH') AND p.discount_code_type = 'LANDINGPAGE' 
			AND (d.landing_page_referrer IS NULL)
);