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

-- Create function to automatically update date_updated
CREATE OR REPLACE FUNCTION update_messages_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update date_updated on UPDATE
CREATE TRIGGER update_messages_date_updated
    BEFORE UPDATE ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_messages_updated_at();

-- Update existing records to set date_created if NULL (for existing data)
-- This handles the case where there might be existing messages without date_created
UPDATE messages
SET date_created = CURRENT_TIMESTAMP
WHERE date_created IS NULL;

-- Optional: Create a function to track message status changes
CREATE OR REPLACE FUNCTION track_message_status_changes()
RETURNS TRIGGER AS $$
BEGIN
    -- If status changed to DELIVERED, set date_delivered
    IF OLD.status IS DISTINCT FROM NEW.status AND NEW.status = 'DELIVERED' AND NEW.date_delivered IS NULL THEN
        NEW.date_delivered = CURRENT_TIMESTAMP;
    END IF;

    -- If status changed to READ, set date_read
    IF OLD.status IS DISTINCT FROM NEW.status AND NEW.status = 'READ' AND NEW.date_read IS NULL THEN
        NEW.date_read = CURRENT_TIMESTAMP;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically set delivery/read dates based on status changes
CREATE TRIGGER track_message_status_changes_trigger
    BEFORE UPDATE ON messages
    FOR EACH ROW
    EXECUTE FUNCTION track_message_status_changes();