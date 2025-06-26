package com.copypoint.api.domain.sale;

public enum SaleStatus {
    PENDING,        // Venta pendiente de procesamiento
    COMPLETED,      // Venta concretada/exitosamente finalizada
    PAYMENT_PENDING, // Pago pendiente (ej: venta aprobada pero falta pago)
    CANCELLED,      // Venta cancelada
    REFUNDED,       // Venta reembolsada
    ON_HOLD,        // Venta en espera (por revisión, etc)
    PARTIALLY_PAID,  // Pago parcial recibido
    FAILED,          // Venta fallida (ej: pago rechazado)
    EXPIRED
}
