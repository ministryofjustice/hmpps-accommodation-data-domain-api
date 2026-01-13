CREATE TABLE proposed_accommodation(
    id                      UUID not null,
    address                 TEXT not null,
    approved                BOOLEAN,
    created_at              TIMESTAMP WITH TIME ZONE default now(),
    last_updated_at         TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY(id)
);