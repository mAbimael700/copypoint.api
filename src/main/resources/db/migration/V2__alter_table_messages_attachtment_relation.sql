-- V1__add_attachment_fields_and_message_relation.sql
-- Migración para agregar campos nuevos a attachments y relación con messages

-- 1. Agregar nuevas columnas a la tabla attachments
ALTER TABLE attachments
ADD COLUMN media_sid VARCHAR(100),
ADD COLUMN message_id BIGINT,
ADD COLUMN download_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN download_attempts INTEGER DEFAULT 0,
ADD COLUMN last_download_attempt TIMESTAMP,
ADD COLUMN download_error_message VARCHAR(500),
ADD COLUMN file_size_bytes BIGINT,
ADD COLUMN mime_type VARCHAR(100),
ADD COLUMN date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN date_updated TIMESTAMP,
ADD COLUMN date_downloaded TIMESTAMP;

-- 2. Agregar constraint para el enum download_status
ALTER TABLE attachments
ADD CONSTRAINT check_download_status
CHECK (download_status IN ('PENDING', 'DOWNLOADING', 'DOWNLOADED', 'FAILED', 'UNAVAILABLE'));

-- 3. Agregar foreign key constraint para message_id
ALTER TABLE attachments
ADD CONSTRAINT fk_attachment_message
FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE;

-- 4. Crear índices para mejorar performance
CREATE INDEX idx_attachments_message_id ON attachments(message_id);
CREATE INDEX idx_attachments_download_status ON attachments(download_status);
CREATE INDEX idx_attachments_media_sid ON attachments(media_sid);


-- 6. Comentarios para documentar los cambios
COMMENT ON COLUMN attachments.media_sid IS 'ID del media en WhatsApp/Twilio';
COMMENT ON COLUMN attachments.message_id IS 'Referencia al mensaje que contiene este attachment';
COMMENT ON COLUMN attachments.download_status IS 'Estado de descarga del attachment: PENDING, DOWNLOADING, DOWNLOADED, FAILED, UNAVAILABLE';
COMMENT ON COLUMN attachments.download_attempts IS 'Número de intentos de descarga realizados';
COMMENT ON COLUMN attachments.last_download_attempt IS 'Fecha del último intento de descarga';
COMMENT ON COLUMN attachments.download_error_message IS 'Mensaje de error si la descarga falló';
COMMENT ON COLUMN attachments.file_size_bytes IS 'Tamaño del archivo en bytes';
COMMENT ON COLUMN attachments.mime_type IS 'Tipo MIME del archivo';
COMMENT ON COLUMN attachments.date_created IS 'Fecha de creación del registro';
COMMENT ON COLUMN attachments.date_updated IS 'Fecha de última actualización del registro';
COMMENT ON COLUMN attachments.date_downloaded IS 'Fecha cuando se completó la descarga';