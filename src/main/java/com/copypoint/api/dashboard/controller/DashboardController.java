package com.copypoint.api.dashboard.controller;

import com.copypoint.api.dashboard.dto.*;
import com.copypoint.api.dashboard.service.DashboardAnalyticsService;
import com.copypoint.api.dashboard.util.DashboardValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard Analytics", description = "Endpoints para análisis y métricas de la aplicación")
@Slf4j
public class DashboardController {

    @Autowired
    private DashboardAnalyticsService analyticsService;

    // === DASHBOARD DE VENTAS GENERALES ===

    @GetMapping("/sales/timeline")
    @Operation(summary = "Obtener línea de tiempo de ventas",
            description = "Devuelve las ventas agrupadas por día en un rango de fechas")
    public ResponseEntity<SalesTimelineResponse> getSalesTimeline(
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Fecha de fin (YYYY-MM-DD)", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo timeline de ventas desde {} hasta {}", startDate, endDate);

        SalesTimelineResponse response = analyticsService.getSalesTimeline(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales/by-copypoint")
    @Operation(summary = "Obtener ventas por copypoint",
            description = "Devuelve las ventas agrupadas por ubicación/copypoint")
    public ResponseEntity<SalesByCopypointResponse> getSalesByCopypoint(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo ventas por copypoint desde {} hasta {}", startDate, endDate);

        SalesByCopypointResponse response = analyticsService.getSalesByCopypoint(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // === DASHBOARD DE ESTADOS DE PAGOS ===

    @GetMapping("/payments/status-distribution")
    @Operation(summary = "Distribución de estados de pagos",
            description = "Devuelve la distribución porcentual de estados de pagos")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatusDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo distribución de estados de pago desde {} hasta {}", startDate, endDate);

        PaymentStatusResponse response = analyticsService.getPaymentStatusDistribution(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/attempts-by-status")
    @Operation(summary = "Intentos de pago por estado",
            description = "Devuelve los intentos de pago agrupados por estado")
    public ResponseEntity<PaymentAttemptsResponse> getPaymentAttemptsByStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo intentos de pago por estado desde {} hasta {}", startDate, endDate);

        PaymentAttemptsResponse response = analyticsService.getPaymentAttemptsByStatus(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // === DASHBOARD DE MÉTODOS DE PAGO ===

    @GetMapping("/payments/method-revenue")
    @Operation(summary = "Ingresos por método de pago",
            description = "Devuelve los ingresos generados por cada método de pago")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethodRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo ingresos por método de pago desde {} hasta {}", startDate, endDate);

        PaymentMethodResponse response = analyticsService.getPaymentMethodRevenue(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/method-distribution")
    @Operation(summary = "Distribución de uso de métodos de pago",
            description = "Devuelve la distribución porcentual del uso de métodos de pago")
    public ResponseEntity<PaymentMethodDistributionResponse> getPaymentMethodDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo distribución de métodos de pago desde {} hasta {}", startDate, endDate);

        PaymentMethodDistributionResponse response = analyticsService.getPaymentMethodDistribution(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // === DASHBOARD DE SERVICIOS MÁS VENDIDOS ===

    @GetMapping("/services/top-services")
    @Operation(summary = "Servicios más vendidos",
            description = "Devuelve los servicios ordenados por cantidad vendida")
    public ResponseEntity<TopServicesResponse> getTopServices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Límite de resultados (máximo 100)", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        DashboardValidationUtils.validateLimit(limit);
        log.info("Obteniendo top {} servicios desde {} hasta {}", limit, startDate, endDate);

        TopServicesResponse response = analyticsService.getTopServices(startDate, endDate, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/services/trends")
    @Operation(summary = "Tendencias de servicios específicos",
            description = "Devuelve la evolución temporal de servicios específicos")
    public ResponseEntity<ServiceTrendResponse> getServiceTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "IDs de servicios a consultar (máximo 10)")
            @RequestParam List<Long> serviceIds) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        DashboardValidationUtils.validateServiceIds(serviceIds);
        log.info("Obteniendo tendencias de servicios {} desde {} hasta {}", serviceIds, startDate, endDate);

        ServiceTrendResponse response = analyticsService.getServiceTrends(startDate, endDate, serviceIds);
        return ResponseEntity.ok(response);
    }

    // === DASHBOARD DE PERFORMANCE POR COPYPOINT ===

    @GetMapping("/copypoints/performance")
    @Operation(summary = "Performance por copypoint",
            description = "Devuelve métricas de rendimiento por ubicación")
    public ResponseEntity<CopypointPerformanceResponse> getCopypointPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo performance por copypoint desde {} hasta {}", startDate, endDate);

        CopypointPerformanceResponse response = analyticsService.getCopypointPerformance(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/copypoints/trends")
    @Operation(summary = "Tendencias por copypoint",
            description = "Devuelve la evolución temporal de ventas por copypoint")
    public ResponseEntity<CopypointTrendResponse> getCopypointTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo tendencias por copypoint desde {} hasta {}", startDate, endDate);

        CopypointTrendResponse response = analyticsService.getCopypointTrends(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // === ENDPOINTS DE CONVENIENCIA ===

    @GetMapping("/summary")
    @Operation(summary = "Resumen general del dashboard",
            description = "Devuelve un resumen con las métricas principales de todos los dashboards")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Si no se proporcionan fechas, usar el último mes
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusMonths(1);
        }

        DashboardValidationUtils.validateDateRange(startDate, endDate);
        log.info("Obteniendo resumen del dashboard desde {} hasta {}", startDate, endDate);

        // Obtener datos resumidos
        SalesTimelineResponse salesTimeline = analyticsService.getSalesTimeline(startDate, endDate);
        PaymentStatusResponse paymentStatus = analyticsService.getPaymentStatusDistribution(startDate, endDate);
        TopServicesResponse topServices = analyticsService.getTopServices(startDate, endDate, 5);
        CopypointPerformanceResponse copypointPerformance = analyticsService.getCopypointPerformance(startDate, endDate);

        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                salesTimeline.metrics(),
                paymentStatus.metrics(),
                topServices.metrics().top5Services(),
                copypointPerformance.metrics()
        );

        return ResponseEntity.ok(summary);
    }

    // Endpoints con rangos predefinidos
    @GetMapping("/sales/timeline/last-week")
    @Operation(summary = "Ventas de la última semana")
    public ResponseEntity<SalesTimelineResponse> getSalesTimelineLastWeek() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        return getSalesTimeline(startDate, endDate);
    }

    @GetMapping("/sales/timeline/last-month")
    @Operation(summary = "Ventas del último mes")
    public ResponseEntity<SalesTimelineResponse> getSalesTimelineLastMonth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        return getSalesTimeline(startDate, endDate);
    }

    @GetMapping("/sales/timeline/last-year")
    @Operation(summary = "Ventas del último año")
    public ResponseEntity<SalesTimelineResponse> getSalesTimelineLastYear() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);
        return getSalesTimeline(startDate, endDate);
    }
}
