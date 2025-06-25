package com.copypoint.api.domain.modules;

public enum ModuleType {
    SALES("SALES", "Gestión de Ventas"),
    INVENTORY("INVENTORY", "Gestión de Inventario"),
    REPORTS("REPORTS", "Reportes y Estadísticas"),
    USER_MANAGEMENT("USER_MANAGEMENT", "Gestión de Usuarios"),
    COPYPOINT_MANAGEMENT("COPYPOINT_MANAGEMENT","Gestión de Copypoints"),
    STORE_MANAGEMENT("STORE_MANAGEMENT", "Gestión de Stores");

    private final String code;
    private final String description;

    ModuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
