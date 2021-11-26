ALTER TABLE discount ADD COLUMN visible_on_eyca BOOLEAN NULL;
UPDATE discount SET visible_on_eyca = false;
ALTER TABLE discount ALTER COLUMN visible_on_eyca SET NOT NULL;