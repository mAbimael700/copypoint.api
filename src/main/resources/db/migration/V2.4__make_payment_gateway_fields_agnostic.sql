-- Script de migración para hacer la tabla agnóstica
-- Ejecutar en tu base de datos

-- 1. Actualizar tabla payments
ALTER TABLE payments
ADD COLUMN gateway_intent_id VARCHAR(255),
ADD COLUMN gateway_payment_id VARCHAR(255),
ADD COLUMN gateway_transaction_id VARCHAR(255);

-- 2. Actualizar tabla payment_methods
ALTER TABLE payment_methods
ADD COLUMN gateway VARCHAR(50),
ADD COLUMN gateway_method_type VARCHAR(100),
ADD COLUMN configuration JSON,
ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- 3. Crear índices para mejorar performance
CREATE INDEX idx_payments_gateway_intent_id ON payments(gateway_intent_id);
CREATE INDEX idx_payments_gateway_payment_id ON payments(gateway_payment_id);
CREATE INDEX idx_payments_gateway_transaction_id ON payments(gateway_transaction_id);
CREATE INDEX idx_payment_methods_gateway ON payment_methods(gateway);

-- 4. Actualizar payment_methods existentes para MercadoPago
UPDATE payment_methods
SET gateway = 'mercadopago',
    gateway_method_type = CASE
        WHEN LOWER(description) LIKE '%digital%wallet%' THEN 'digital_wallet'
        WHEN LOWER(description) LIKE '%credit%card%' THEN 'credit_card'
        WHEN LOWER(description) LIKE '%debit%card%' THEN 'debit_card'
        WHEN LOWER(description) LIKE '%bank%transfer%' THEN 'bank_transfer'
        ELSE 'other'
    END,
    is_active = TRUE
WHERE gateway IS NULL;

-- 5. Ejemplos de datos para otras pasarelas (opcional)
-- INSERT INTO payment_methods (description, gateway, gateway_method_type, is_active) VALUES
-- ('Stripe Credit Card', 'stripe', 'card', TRUE),
-- ('Stripe SEPA', 'stripe', 'sepa_debit', TRUE),
-- ('PayPal', 'paypal', 'paypal', TRUE),
-- ('Square', 'square', 'card', TRUE),
-- ('Conekta Card', 'conekta', 'card', TRUE);

-- 6. Comentarios sobre migración de datos existentes
-- Si tienes payments existentes y quieres migrar los gateway_id como gateway_intent_id:
UPDATE payments
SET gateway_intent_id = gateway_id
 WHERE gateway_id IS NOT NULL
  AND gateway_intent_id IS NULL
  AND gateway_id LIKE '%-%'; -- MercadoPago Preference IDs tienen guiones
