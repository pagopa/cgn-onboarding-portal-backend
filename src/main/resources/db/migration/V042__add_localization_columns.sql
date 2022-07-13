ALTER TABLE profile
    ADD COLUMN name_en VARCHAR(100);

ALTER TABLE profile
    ADD COLUMN name_de VARCHAR(100);

ALTER TABLE profile
    ADD COLUMN description_en VARCHAR(300) NOT NULL DEFAULT '-';

ALTER TABLE profile
    ADD COLUMN description_de VARCHAR(300) NOT NULL DEFAULT '-';

ALTER TABLE discount
    ADD COLUMN name_en VARCHAR(100) NOT NULL DEFAULT '-';

ALTER TABLE discount
    ADD COLUMN name_de VARCHAR(100) NOT NULL DEFAULT '-';

ALTER TABLE discount
    ADD COLUMN description_en VARCHAR(250);

ALTER TABLE discount
    ADD COLUMN description_de VARCHAR(250);

ALTER TABLE discount
    ADD COLUMN condition_en VARCHAR(200);

ALTER TABLE discount
    ADD COLUMN condition_de VARCHAR(200);
