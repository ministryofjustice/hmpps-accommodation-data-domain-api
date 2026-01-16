ALTER TABLE outbox_event DROP COLUMN processed;

ALTER TABLE outbox_event ADD COLUMN processed_status VARCHAR(20) NOT NULL default 'PENDING';
ALTER TABLE outbox_event ADD COLUMN processed_at TIMESTAMP WITH TIME ZONE;
