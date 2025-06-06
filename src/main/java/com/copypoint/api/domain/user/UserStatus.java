package com.copypoint.api.domain.user;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    UNDER_REVIEW,
    BANNED,
    PENDING_VERIFICATION, // Registrado pero email/teléfono sin verificar
    SUSPENDED,           // Suspendido por violación de términos
    DEACTIVATED,         // Desactivado por el propio usuario
    LOCKED,              // Bloqueado por intentos fallidos de login
    PENDING_APPROVAL,    // Esperando aprobación manual
    EXPIRED,             // Cuenta expirada (para cuentas temporales/trial)
    LIMITED              // Acceso limitado (funcionalidades restringidas)
}
