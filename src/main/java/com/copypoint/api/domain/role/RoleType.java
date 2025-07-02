package com.copypoint.api.domain.role;

public enum RoleType {
    // Roles para Employee
    EMPLOYEE_BASIC("EMPLOYEE_BASIC", "Empleado Básico"),
    EMPLOYEE_SUPERVISOR("EMPLOYEE_SUPERVISOR", "Empleado Supervisor"),

    // Roles para Administrator
    STORE_MANAGER("STORE_MANAGER", "Administrador Gerente"),
    STORE_OWNER("STORE_OWNER", "Administrador Propietario"),
    COPYPOINT_MANAGER("COPYPOINT_MANAGER","Administrador copypoint" );

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
