package com.copypoint.api.domain.employee.dto;

import com.copypoint.api.domain.employee.Employee;

public record EmployeeDTO() {
    public EmployeeDTO(Employee newEmployee) {
        this();
    }
}
