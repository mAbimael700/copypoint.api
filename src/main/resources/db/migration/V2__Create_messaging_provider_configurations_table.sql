-- Flyway Migration Script
-- Version: V2__Create_messaging_provider_configurations_table.sql
-- Description: Creates the messaging_provider_configurations table with inheritance support

CREATE TABLE messaging_provider_configurations (
    id BIGSERIAL PRIMARY KEY,
    provider_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    display_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on provider_type for better performance on inheritance queries
CREATE INDEX idx_messaging_provider_config_provider_type ON messaging_provider_configurations(provider_type);

-- Create index on is_active for filtering active configurations
CREATE INDEX idx_messaging_provider_config_is_active ON messaging_provider_configurations(is_active);

-- Add comments for better documentation
COMMENT ON TABLE messaging_provider_configurations IS 'Base table for messaging provider configurations using JPA inheritance';

COMMENT ON COLUMN messaging_provider_configurations.id IS 'Primary key';
COMMENT ON COLUMN messaging_provider_configurations.provider_type IS 'Discriminator column for JPA inheritance';
COMMENT ON COLUMN messaging_provider_configurations.is_active IS 'Flag to indicate if configuration is active';
COMMENT ON COLUMN messaging_provider_configurations.display_name IS 'Human readable name for the configuration';

-- Create function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_messaging_provider_configurations_updated_at
    BEFORE UPDATE ON messaging_provider_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();