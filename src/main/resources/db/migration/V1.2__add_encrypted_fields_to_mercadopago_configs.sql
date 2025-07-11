-- Drop the previous version of the table
DROP TABLE IF EXISTS mercadopago_configs CASCADE;

-- Create the new table definition
CREATE TABLE mercadopago_configs (
    id BIGSERIAL PRIMARY KEY,
    copypoint_id BIGINT REFERENCES copypoints(id),
    access_token_encrypted VARCHAR(1000) NOT NULL,
    public_key_encrypted VARCHAR(1000) NOT NULL,
    client_secret_encrypted VARCHAR(1000) NOT NULL,
    webhook_secret_encrypted VARCHAR(1000) NOT NULL,
    client_id VARCHAR(200) NOT NULL,
    is_sandbox BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    vendor_email VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optional but useful index for filtering
CREATE INDEX idx_copypoint_active ON mercadopago_configs(copypoint_id, is_active);

-- Optional unique constraint to ensure one active config per copypoint
CREATE UNIQUE INDEX unique_active_copypoint ON mercadopago_configs(is_active, copypoint_id);
