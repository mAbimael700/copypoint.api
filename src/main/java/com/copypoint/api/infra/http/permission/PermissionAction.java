package com.copypoint.api.infra.http.permission;

/**
 * Define las acciones específicas que se pueden realizar en cada módulo
 * Permite permisos más granulares como READ, WRITE, DELETE
 */
public enum PermissionAction {
    /**
     * Permiso de lectura - Ver/consultar información
     */
    READ("READ", "Lectura"),

    /**
     * Permiso de escritura - Crear y editar información
     */
    WRITE("WRITE", "Escritura"),

    /**
     * Permiso de eliminación - Eliminar información
     */
    DELETE("DELETE", "Eliminación"),

    /**
     * Acceso completo - Todas las acciones
     */
    FULL("FULL", "Acceso Completo");

    private final String code;
    private final String displayName;

    PermissionAction(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
