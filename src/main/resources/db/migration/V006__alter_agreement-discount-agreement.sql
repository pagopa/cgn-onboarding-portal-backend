ALTER TABLE agreement
    ADD COLUMN assignee VARCHAR(100);
ALTER TABLE agreement
    ADD COLUMN request_approval_time TIMESTAMPTZ;


ALTER TABLE discount ALTER COLUMN discount_value TYPE INTEGER;
ALTER TABLE agreement
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

