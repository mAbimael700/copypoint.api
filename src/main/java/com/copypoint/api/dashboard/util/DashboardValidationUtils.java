package com.copypoint.api.dashboard.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DashboardValidationUtils {
    private static final long MAX_DATE_RANGE_DAYS = 1825; // 5 años

    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser futura");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_DATE_RANGE_DAYS) {
            throw new IllegalArgumentException("El rango de fechas no puede ser mayor a 5 años");
        }
    }

    public static void validateLimit(Integer limit) {
        if (limit != null && (limit < 1 || limit > 100)) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100");
        }
    }

    public static void validateServiceIds(List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un ID de servicio");
        }

        if (serviceIds.size() > 10) {
            throw new IllegalArgumentException("No se pueden consultar más de 10 servicios a la vez");
        }

        boolean hasInvalidIds = serviceIds.stream().anyMatch(id -> id == null || id <= 0);
        if (hasInvalidIds) {
            throw new IllegalArgumentException("Todos los IDs de servicio deben ser números positivos");
        }
    }
}
