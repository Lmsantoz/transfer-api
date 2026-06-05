CREATE TABLE transfer (
    id UUID PRIMARY KEY,
    origin_id UUID NOT NULL,
    FOREIGN KEY (origin_id) REFERENCES accounts(id),
    destination_id UUID NOT NULL,
    FOREIGN KEY (destination_id) REFERENCES accounts(id),
    amount DECIMAL(8, 2) NOT NULL,
    transfer_date TIMESTAMP NOT NULL,
    status VARCHAR NOT NULL
);