package com.copypoint.api.domain.paymentAttempt;

public enum PaymentAttemptStatus {
    // Estados iniciales
    INITIATED,           // Intento iniciado, preparando pago
    PENDING,             // Pendiente de procesamiento

    // Estados de procesamiento
    PROCESSING,          // En proceso en la pasarela
    AWAITING_CONFIRMATION, // Esperando confirmación del usuario (3DS, etc.)
    REQUIRES_ACTION,     // Requiere acción adicional del usuario

    // Estados finales exitosos
    SUCCEEDED,           // Pago exitoso
    COMPLETED,           // Completado y confirmado

    // Estados de falla
    FAILED,              // Falló el intento de pago
    DECLINED,            // Rechazado por el banco/tarjeta
    CANCELLED,           // Cancelado por el usuario
    ABANDONED,           // Usuario abandonó el proceso
    EXPIRED,             // Sesión de pago expirada

    // Estados específicos de pasarelas
    INSUFFICIENT_FUNDS,  // Fondos insuficientes
    INVALID_CARD,        // Tarjeta inválida o expirada
    AUTHENTICATION_FAILED, // Falló la autenticación (3DS, etc.)
    FRAUD_DETECTED,      // Detectado como fraudulento
    BLOCKED_BY_GATEWAY,  // Bloqueado por la pasarela
    NETWORK_ERROR,       // Error de red/conectividad

    // Estados de seguimiento
    REFUND_PENDING,      // Reembolso en proceso
    REFUNDED,            // Reembolsado exitosamente
    DISPUTED,            // En disputa/chargeback

    // Estados administrativos
    UNDER_REVIEW,        // En revisión manual
    FLAGGED              // Marcado para revisión
}
