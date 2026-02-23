DROP VIEW IF EXISTS eyca_data_export;

CREATE VIEW eyca_data_export AS
SELECT
    distinct on (d.discount_k) "discount_id",
    row_number() over () as "id",
	d.state as "state",
    REPLACE(REPLACE(cat.categories :: text, '{', ''), '}', '') as "categories",
    p.profile_k as "profile_id",
	COALESCE(NULLIF(p.name,''), NULLIF(p.full_name,'')) AS vendor,
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
            END
        )
    ) as "text_local",
    '' as "email",
    '' as "phone",
    p.website_url as "web",
    '' as "tags",
    'https://io-p-itn-cgn-pe-frontend-assets-fde-01-e6cbebfwdrhcaqed.a02.azurefd.net/' || ag.image_url as "image",
	l.live as "live",
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
    d.static_code as "static_code",
    d.landing_page_url as "landing_page_url",
    d.landing_page_referrer as "landing_page_referrer",
    d.eyca_landing_page_url as "eyca_landing_page_url",
    d.eyca_email_update_required as "eyca_email_update_required",
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
AND (l.live = 'Y'
	OR (l.live = 'N'
		AND d.eyca_update_id IS NOT NULL))
AND l.discount_id = d.discount_k
AND (
    sales_channel ='OFFLINE'
    OR
    (p.sales_channel IN ('ONLINE', 'BOTH')
		AND (
            p.discount_code_type IN ('STATIC', 'BUCKET')
            OR
            (p.discount_code_type = 'LANDINGPAGE' AND d.eyca_landing_page_url IS NOT NULL)
        ))
)