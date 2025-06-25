package com.copypoint.api.domain.employeerole;

import java.time.LocalDateTime;

public interface EmployeeRolePermissionProjection {
    Long getEmployeeRoleId();
    Long getEmployeeId();
    Long getCopypointId();
    Long getStoreId();
    Long getRoleId();
    LocalDateTime getAddedAt();
    String getRoleName();
    Long getModuleId();
    String getModuleName();
    Boolean getModuleActive();
}
