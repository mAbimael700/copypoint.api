ALTER TABLE messages
DROP CONSTRAINT IF EXISTS messages_status_check;

ALTER TABLE messages
ADD CONSTRAINT messages_status_check
CHECK (((status)::text = ANY (ARRAY[
    ('SENT'::character varying)::text,
    ('DELIVERED'::character varying)::text,
    ('READ'::character varying)::text,
    ('FAILED'::character varying)::text,
    ('QUEUED'::character varying)::text,
    ('SENDING'::character varying)::text,
    ('UNDELIVERED'::character varying)::text,
    ('UNKNOWN'::character varying)::text,
    ('PARTIALLY_DELIVERED'::character varying)::text,
    ('REJECTED'::character varying)::text,
    ('SCHEDULED'::character varying)::text,
    ('CANCELLED'::character varying)::text,
    ('RECEIVED'::character varying)::text
])));
