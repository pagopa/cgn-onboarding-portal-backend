ALTER TYPE discount_state_enum RENAME VALUE 'REJECTED' TO 'SUSPENDED';
ALTER TABLE discount ADD COLUMN suspended_reason_message VARCHAR(250);