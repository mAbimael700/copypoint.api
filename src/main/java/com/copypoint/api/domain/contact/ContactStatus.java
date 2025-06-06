package com.copypoint.api.domain.contact;

public enum ContactStatus {
    ACTIVE,              // Contacto activo y válido
    INACTIVE,            // Contacto inactivo
    VERIFIED,            // Contacto verificado (email/teléfono confirmado)
    UNVERIFIED,          // Contacto sin verificar
    BOUNCED,             // Email rebotado o número inválido
    OPTED_OUT,           // Usuario se dio de baja de comunicaciones
    BLOCKED,             // Contacto bloqueado por spam u otros motivos
    DUPLICATE,           // Contacto marcado como duplicado
    DELETED              // Soft delete
}
