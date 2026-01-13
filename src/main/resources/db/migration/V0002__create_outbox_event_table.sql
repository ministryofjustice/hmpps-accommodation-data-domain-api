CREATE TABLE outbox_event (
    id UUID,
    aggregate_id UUID not null,
    aggregate_type TEXT not null,
    domain_event_type TEXT not null,
    payload TEXT not null,
    created_at TIMESTAMP WITH TIME ZONE default now(),
    processed BOOLEAN not null default false,
    PRIMARY KEY(id)
);
