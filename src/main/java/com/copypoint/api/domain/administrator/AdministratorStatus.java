package com.copypoint.api.domain.administrator;

public enum AdministratorStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,           // Suspendido temporalmente
    PENDING_ACTIVATION,  // Pendiente de activación inicial
    LOCKED              // Bloqueado por intentos fallidos de login
}
