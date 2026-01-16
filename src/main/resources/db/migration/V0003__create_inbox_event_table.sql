CREATE TABLE inbox_event (
    id UUID PRIMARY KEY,
    event_type TEXT NOT NULL,
    event_details_url TEXT NOT NULL,
    event_occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE default now(),
    processed BOOLEAN NOT NULL DEFAULT FALSE
);
