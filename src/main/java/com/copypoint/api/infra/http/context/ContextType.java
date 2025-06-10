package com.copypoint.api.infra.http.context;

/**
 * Define los diferentes contextos donde se pueden aplicar permisos
 */
public enum ContextType {
    /**
     * Contexto de tienda - Los permisos se aplican a nivel de Store
     * Administradores tienen roles en Stores específicos
     */
    STORE,

    /**
     * Contexto de copypoint - Los permisos se aplican a nivel de Copypoint
     * Empleados tienen roles en Copypoints específicos
     */
    COPYPOINT,

    /**
     * Contexto global - Operaciones que no requieren contexto específico
     * Ejemplo: ver perfil propio, cambiar contraseña, etc.
     */
    GLOBAL
}
