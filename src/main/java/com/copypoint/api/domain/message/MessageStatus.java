package com.copypoint.api.domain.message;

public enum MessageStatus {
    SENT,
    DELIVERED,
    READ,
    FAILED,
    QUEUED,              // En cola para envío
    SENDING,             // En proceso de envío
    UNDELIVERED,         // No entregado (diferente a failed)
    UNKNOWN,             // Estado desconocido
    PARTIALLY_DELIVERED, // Entregado parcialmente (para mensajes largos)
    REJECTED,            // Rechazado por el proveedor
    SCHEDULED,           // Programado para envío futuro
    CANCELLED            // Cancelado antes del envío
}
