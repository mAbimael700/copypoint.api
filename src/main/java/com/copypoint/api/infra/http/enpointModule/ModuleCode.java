package com.copypoint.api.infra.http.enpointModule;

import lombok.Getter;

/**
 * Define todos los módulos del sistema que pueden requerir permisos específicos
 * Estos códigos deben coincidir con los nombres de módulos en la BD
 */
@Getter
public enum ModuleCode {
    // === GESTIÓN DE USUARIOS Y ROLES ===
    /**
     * Gestión de usuarios - Crear, editar, eliminar usuarios
     * Aplica a: Administradores y empleados
     */
    USER_MANAGEMENT("USER_MANAGEMENT", "Gestión de Usuarios", "Crear, editar y eliminar usuarios del sistema"),

    /**
     * Gestión de roles - Asignar/remover roles, gestionar permisos
     * Aplica a: Roles de administradores y empleados
     */
    ROLE_MANAGEMENT("ROLE_MANAGEMENT", "Gestión de Roles", "Asignar y gestionar roles y permisos"),

    // === GESTIÓN DE TIENDA (STORE LEVEL) ===
    /**
     * Gestión de tiendas - Configuración general de la tienda
     * Aplica a: Configuración, información básica de la tienda
     */
    STORE_MANAGEMENT("STORE_MANAGEMENT", "Gestión de Tiendas", "Configuración y administración general de tiendas"),

    /**
     * Gestión de materiales - CRUD de materiales de la tienda
     * Aplica a: Inventario de materiales, precios, proveedores
     */
    MATERIALS_MANAGEMENT("MATERIALS_MANAGEMENT", "Gestión de Materiales", "Administración del catálogo de materiales"),

    /**
     * Gestión de servicios - CRUD de servicios ofrecidos
     * Aplica a: Catálogo de servicios, precios, configuración
     */
    SERVICES_MANAGEMENT("SERVICES_MANAGEMENT", "Gestión de Servicios", "Administración del catálogo de servicios"),

    /**
     * Gestión de clientes - CRUD de información de clientes
     * Aplica a: Base de datos de clientes, historial, preferencias
     */
    CLIENTS_MANAGEMENT("CLIENTS_MANAGEMENT", "Gestión de Clientes", "Administración de la base de datos de clientes"),

    /**
     * Gestión de administradores - Asignar administradores a la tienda
     * Aplica a: Agregar/remover administradores de la tienda
     */
    ADMINISTRATORS_MANAGEMENT("ADMINISTRATORS_MANAGEMENT", "Gestión de Administradores", "Administración de roles administrativos"),

    // === GESTIÓN DE COPYPOINT (COPYPOINT LEVEL) ===
    /**
     * Gestión de copypoints - Configuración del copypoint
     * Aplica a: Información básica, configuración operativa
     */
    COPYPOINT_MANAGEMENT("COPYPOINT_MANAGEMENT", "Gestión de Copypoints", "Configuración y administración de copypoints"),

    /**
     * Gestión de ventas - Procesar ventas, consultar historial
     * Aplica a: POS, historial de ventas, devoluciones
     */
    SALES_MANAGEMENT("SALES_MANAGEMENT", "Gestión de Ventas", "Procesamiento y administración de ventas"),

    /**
     * Gestión de inventario - Control de stock del copypoint
     * Aplica a: Inventario local, transferencias, ajustes
     */
    INVENTORY_MANAGEMENT("INVENTORY_MANAGEMENT", "Gestión de Inventario", "Control de inventario y stock"),

    /**
     * Gestión de empleados - Administrar empleados del copypoint
     * Aplica a: Asignar/remover empleados del copypoint
     */
    EMPLOYEES_MANAGEMENT("EMPLOYEES_MANAGEMENT", "Gestión de Empleados", "Administración del personal del copypoint"),

    // === REPORTES Y ANÁLISIS ===
    /**
     * Gestión de reportes - Generar y consultar reportes
     * Aplica a: Reportes de ventas, inventario, rendimiento
     */
    REPORTS_MANAGEMENT("REPORTS_MANAGEMENT", "Gestión de Reportes", "Generación y consulta de reportes del sistema"),

    /**
     * Análisis y estadísticas - Dashboard, métricas, KPIs
     * Aplica a: Analytics, dashboard ejecutivo, métricas de rendimiento
     */
    ANALYTICS_MANAGEMENT("ANALYTICS_MANAGEMENT", "Análisis y Estadísticas", "Acceso a análisis y métricas del negocio"),

    // === CONFIGURACIÓN Y SISTEMA ===
    /**
     * Configuración del sistema - Parámetros generales
     * Aplica a: Configuraciones técnicas, parámetros del sistema
     */
    SYSTEM_CONFIGURATION("SYSTEM_CONFIGURATION", "Configuración del Sistema", "Administración de configuraciones técnicas"),

    /**
     * Auditoría y logs - Consultar logs del sistema
     * Aplica a: Trazabilidad, logs de auditoría, historial de cambios
     */
    AUDIT_MANAGEMENT("AUDIT_MANAGEMENT", "Gestión de Auditoría", "Acceso a logs y trazabilidad del sistema");


    private final String code;
    private final String displayName;
    private final String description;

    ModuleCode(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Busca un ModuleCode por su código
     *
     * @param code El código del módulo
     * @return El ModuleCode correspondiente
     * @throws IllegalArgumentException si no existe el código
     */
    public static ModuleCode fromCode(String code) {
        for (ModuleCode module : values()) {
            if (module.code.equals(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("No existe el módulo con código: " + code);
    }

    /**
     * Verifica si un código de módulo existe
     *
     * @param code El código a verificar
     * @return true si existe, false en caso contrario
     */
    public static boolean exists(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
