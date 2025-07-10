CREATE TABLE mercadopago_configs (
    id BIGSERIAL PRIMARY KEY,
    copypoint_id BIGINT REFERENCES copypoints(id),
    access_token VARCHAR(500) NOT NULL,
    public_key VARCHAR(500) NOT NULL,
    client_id VARCHAR(200),
    client_secret VARCHAR(500),
    webhook_secret VARCHAR(200),
    is_sandbox BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    vendor_email VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_copypoint_active ON mercadopago_configs(copypoint_id, is_active);

CREATE UNIQUE INDEX unique_active_copypoint ON mercadopago_configs(is_active, copypoint_id);
