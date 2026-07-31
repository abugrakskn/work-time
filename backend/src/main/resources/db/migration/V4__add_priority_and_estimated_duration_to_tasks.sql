ALTER TABLE tasks
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

ALTER TABLE tasks
    ADD COLUMN estimated_duration_minutes INTEGER;