-- Flyway Migration Script
-- Version: V2.1__Create_twilio_configurations_table.sql
-- Description: Creates the twilio_configurations table for JPA JOINED inheritance

CREATE TABLE twilio_configurations (
    id BIGINT PRIMARY KEY,
    account_sid VARCHAR(1000) NOT NULL,
    auth_token VARCHAR(1000) NOT NULL,
    webhook_url VARCHAR(255),
    status_callback_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint to parent table
    CONSTRAINT fk_twilio_config_messaging_provider
        FOREIGN KEY (id) REFERENCES messaging_provider_configurations(id)
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_twilio_config_webhook_url ON twilio_configurations(webhook_url);
CREATE INDEX idx_twilio_config_status_callback_url ON twilio_configurations(status_callback_url);

-- Add comments for better documentation
COMMENT ON TABLE twilio_configurations IS 'Twilio-specific configuration extending MessagingProviderConfig';

COMMENT ON COLUMN twilio_configurations.id IS 'Primary key and foreign key to messaging_provider_configurations';
COMMENT ON COLUMN twilio_configurations.account_sid IS 'Encrypted Twilio Account SID (AES encrypted)';
COMMENT ON COLUMN twilio_configurations.auth_token IS 'Encrypted Twilio Auth Token (AES encrypted)';
COMMENT ON COLUMN twilio_configurations.webhook_url IS 'Twilio webhook URL for incoming messages';
COMMENT ON COLUMN twilio_configurations.status_callback_url IS 'Twilio status callback URL for message delivery status';