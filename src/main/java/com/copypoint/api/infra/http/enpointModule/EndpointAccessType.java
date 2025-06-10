package com.copypoint.api.infra.http.enpointModule;

/**
 * Define los diferentes tipos de acceso para endpoints
 */
public enum EndpointAccessType {
    /**
     * Endpoints completamente públicos - No requieren autenticación ni autorización
     * Ejemplo: /api/login, /api/health, /api/stores/public-info
     */
    PUBLIC,

    /**
     * Endpoints que solo requieren autenticación válida (JWT válido)
     * No requieren validación de roles/permisos específicos
     * Ejemplo: /api/users/profile, /api/users/change-password
     */
    AUTHENTICATED,

    /**
     * Endpoints que requieren autenticación + autorización por módulos
     * Requieren validación de roles y permisos específicos
     * Ejemplo: /api/stores/{id}/materials, /api/copypoints/{id}/sales
     */
    AUTHORIZED
}
