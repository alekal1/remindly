CREATE TABLE remindly.event (
    id             BIGSERIAL    NOT NULL,
    type           VARCHAR(255) NOT NULL,
    scheduled_at   TIMESTAMP    NOT NULL,
    sent_at        TIMESTAMP,
    CONSTRAINT event_pk PRIMARY KEY (id)
);

ALTER TABLE remindly.event
OWNER TO remindly;

GRANT ALL PRIVILEGES ON TABLE remindly.event TO remindly;