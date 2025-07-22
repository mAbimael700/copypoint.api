-- V1_3__Create_whatsapp_business_configurations.sql
-- Migración para crear la tabla whatsapp_business_configurations
-- Base de datos: PostgreSQL

-- Crear la tabla whatsapp_business_configurations que extiende de messaging_provider_configurations
CREATE TABLE whatsapp_business_configurations (
    id BIGINT NOT NULL,
    access_token_encrypted VARCHAR(500),
    phone_number_id VARCHAR(255) NOT NULL,
    business_account_id VARCHAR(255) NOT NULL,
    webhook_verify_token VARCHAR(255) NOT NULL,
    app_id VARCHAR(255),
    app_secret_encrypted VARCHAR(500),

    -- Clave primaria
    CONSTRAINT pk_whatsapp_business_configurations PRIMARY KEY (id),

    -- Clave foránea hacia la tabla padre
    CONSTRAINT fk_whatsapp_business_configurations_messaging_provider
        FOREIGN KEY (id) REFERENCES messaging_provider_configurations(id)
        ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_whatsapp_business_phone_number_id
    ON whatsapp_business_configurations(phone_number_id);

CREATE INDEX idx_whatsapp_business_account_id
    ON whatsapp_business_configurations(business_account_id);

CREATE INDEX idx_whatsapp_business_app_id
    ON whatsapp_business_configurations(app_id)
    WHERE app_id IS NOT NULL;

-- Comentarios para documentación
COMMENT ON TABLE whatsapp_business_configurations
    IS 'Configuración específica para WhatsApp Business API que extiende messaging_provider_configurations';

COMMENT ON COLUMN whatsapp_business_configurations.access_token_encrypted
    IS 'Token de acceso encriptado para la API de WhatsApp Business';

COMMENT ON COLUMN whatsapp_business_configurations.phone_number_id
    IS 'ID del número de teléfono de WhatsApp Business';

COMMENT ON COLUMN whatsapp_business_configurations.business_account_id
    IS 'ID de la cuenta de negocio de WhatsApp';

COMMENT ON COLUMN whatsapp_business_configurations.webhook_verify_token
    IS 'Token de verificación para webhooks de WhatsApp';

COMMENT ON COLUMN whatsapp_business_configurations.app_id
    IS 'ID de la aplicación de Facebook/Meta (opcional)';

COMMENT ON COLUMN whatsapp_business_configurations.app_secret_encrypted
    IS 'Secreto de la aplicación encriptado (opcional)';