package com.copypoint.api.domain.payment.entity;

public enum PaymentStatus {
    PENDING,             // Pago pendiente/iniciado
    PROCESSING,          // En proceso de validación
    COMPLETED,           // Pago completado exitosamente
    FAILED,              // Pago falló
    CANCELLED,           // Pago cancelado por el usuario
    REFUNDED,            // Pago reembolsado
    PARTIALLY_REFUNDED,  // Reembolso parcial
    DISPUTED,            // Pago en disputa/chargeback
    EXPIRED,             // Pago expirado (para links de pago temporales)
    AUTHORIZED,          // Autorizado pero no capturado
    CAPTURED,            // Capturado después de autorización
    VOIDED,              // Anulado
    REJECTED,            // Rechazado por el banco/procesador
    ON_HOLD,             // En retención por revisión
    APPROVED,
    REQUIRES_ACTION      // Requiere acción adicional (2FA, etc.)
}
