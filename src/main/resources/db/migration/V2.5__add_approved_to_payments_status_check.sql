-- Eliminar la restricción existente (si existe)
ALTER TABLE public.payments
DROP CONSTRAINT IF EXISTS payments_status_check;

-- Crear la nueva restricción con todos los valores incluyendo APPROVED
ALTER TABLE public.payments
ADD CONSTRAINT payments_status_check CHECK (
    status IN (
        'PENDING',
        'PROCESSING',
        'COMPLETED',
        'FAILED',
        'CANCELLED',
        'REFUNDED',
        'PARTIALLY_REFUNDED',
        'DISPUTED',
        'EXPIRED',
        'AUTHORIZED',
        'CAPTURED',
        'VOIDED',
        'REJECTED',
        'ON_HOLD',
        'APPROVED',
        'REQUIRES_ACTION'
    )
);
