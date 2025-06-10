package com.copypoint.api.domain.employee;

public enum EmployeeStatus {
    ACTIVE,
    INACTIVE,
    ON_LEAVE,            // En licencia/vacaciones
    SUSPENDED,           // Suspendido
    TERMINATED,          // Terminado/despedido
    PENDING_START,       // Contratado pero aún no inicia
    PROBATION,           // En período de prueba
    RETIRED              // Jubilado
}
