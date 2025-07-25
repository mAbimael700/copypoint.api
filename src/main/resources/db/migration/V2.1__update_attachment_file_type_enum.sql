-- Migración para agregar nuevos valores al enum AttachmentFileType
-- V2.1__update_attachment_file_type_enum.sql

-- Eliminar el constraint existente
ALTER TABLE public.attachments DROP CONSTRAINT attachments_file_type_check;

-- Agregar el nuevo constraint con todos los valores del enum
ALTER TABLE public.attachments ADD CONSTRAINT attachments_file_type_check
    CHECK (((file_type)::text = ANY ((ARRAY[
        'PDF'::character varying,
        'DOC'::character varying,
        'DOCX'::character varying,
        'PNG'::character varying,
        'JPG'::character varying,
        'PPT'::character varying,
        'ODT'::character varying,
        'TXT'::character varying,
        'IMAGE'::character varying,
        'VIDEO'::character varying,
        'AUDIO'::character varying,
        'DOCUMENT'::character varying,
        'SPREADSHEET'::character varying,
        'PRESENTATION'::character varying,
        'OTHER'::character varying
    ])::text[])));