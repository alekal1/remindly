ALTER TABLE remindly.event
ADD COLUMN parent_event_id BIGINT;

ALTER TABLE remindly.event
ADD CONSTRAINT event_parent_event_id_fk
    FOREIGN KEY (parent_event_id)
    REFERENCES remindly.event(id) ON DELETE CASCADE;
