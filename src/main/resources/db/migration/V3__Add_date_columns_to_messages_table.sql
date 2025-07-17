-- Flyway Migration Script
-- Version: V3__Add_date_columns_to_messages_table.sql
-- Description: Adds date columns to the messages table for message lifecycle tracking

-- Add the new date columns to the messages table
ALTER TABLE messages
ADD COLUMN date_sent TIMESTAMP,
ADD COLUMN date_delivered TIMESTAMP,
ADD COLUMN date_read TIMESTAMP,
ADD COLUMN date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN date_updated TIMESTAMP;

-- Create indexes for better performance on date queries
CREATE INDEX idx_messages_date_sent ON messages(date_sent);
CREATE INDEX idx_messages_date_delivered ON messages(date_delivered);
CREATE INDEX idx_messages_date_read ON messages(date_read);
CREATE INDEX idx_messages_date_created ON messages(date_created);
CREATE INDEX idx_messages_date_updated ON messages(date_updated);

-- Create composite index for common queries (conversation + date_created)
CREATE INDEX idx_messages_conversation_date_created ON messages(conversation_id, date_created);

-- Add comments for better documentation
COMMENT ON COLUMN messages.date_sent IS 'Date when the message was sent from Twilio (official message date)';
COMMENT ON COLUMN messages.date_delivered IS 'Date when the message was delivered to the recipient';
COMMENT ON COLUMN messages.date_read IS 'Date when the message was read by the recipient';
COMMENT ON COLUMN messages.date_created IS 'Date when the message was created in our system';
COMMENT ON COLUMN messages.date_updated IS 'Date of the last update to the message';

-- Update existing records to set date_created if NULL (for existing data)
UPDATE messages
SET date_created = CURRENT_TIMESTAMP
WHERE date_created IS NULL;