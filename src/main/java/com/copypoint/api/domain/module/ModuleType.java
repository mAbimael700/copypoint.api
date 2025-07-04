package com.copypoint.api.domain.module;

public enum ModuleType {
    COPYPOINT_SALES("COPYPOINT_SALES", "Gestión de Ventas en Copypoint"),
    COPYPOINT_INVENTORY("COPYPOINT_INVENTORY", "Gestión de Inventario en Copypoint"),
    COPYPOINT_REPORTS("COPYPOINT_REPORTS", "Reportes y Estadísticas en Copypoit"),
    COPYPOINT_EMPLOYEE_MANAGEMENT("COPYPOINT_EMPLOYEE_MANAGEMENT", "Gestión de Empleados en Copypoint"),
    STORE_INVENTORY("COPYPOINT_INVENTORY", "Gestión de Inventario en Store"),
    STORE_REPORTS("COPYPOINT_REPORTS", "Reportes y Estadísticas en Store"),
    STORE_EMPLOYEE_MANAGEMENT("COPYPOINT_EMPLOYEE_MANAGEMENT", "Gestión de Empleados en Store"),
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
