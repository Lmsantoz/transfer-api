CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    balance DECIMAL(8, 2) NOT NULL,
    number_account VARCHAR(15) NOT NULL,
    client_id UUID NOT NULL,

    FOREIGN KEY (client_id) REFERENCES clients(id)
);