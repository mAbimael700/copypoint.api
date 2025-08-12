package com.copypoint.api.dashboard.service;


import com.copypoint.api.dashboard.dto.*;
import com.copypoint.api.domain.payment.entity.PaymentStatus;
import com.copypoint.api.domain.payment.repository.PaymentRepository;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.copypoint.api.domain.saleprofile.repository.SaleProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardAnalyticsService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private  PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private SaleProfileRepository saleProfileRepository;

    // Dashboard de Ventas Generales
    public SalesTimelineResponse getSalesTimeline(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // Query nativo para obtener ventas por día
        List<Object[]> results = saleRepository.findSalesTimelineData(start, end);

        List<SalesTimelineData> timeline = results.stream()
                .map(row -> new SalesTimelineData(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).intValue()
                ))
                .collect(Collectors.toList());

        SalesMetrics metrics = calculateSalesMetrics(timeline);

        return new SalesTimelineResponse(timeline, metrics);
    }

    public SalesByCopypointResponse getSalesByCopypoint(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = saleRepository.findSalesByCopypoint(start, end);

        List<SalesByCopypointData> salesByLocation = results.stream()
                .map(row -> new SalesByCopypointData(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).intValue()
                ))
                .collect(Collectors.toList());

        SalesMetrics globalMetrics = calculateGlobalMetrics(salesByLocation);

        return new SalesByCopypointResponse(salesByLocation, globalMetrics);
    }

    // Dashboard de Estados de Pagos
    public PaymentStatusResponse getPaymentStatusDistribution(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = paymentRepository.findPaymentStatusDistribution(start, end);

        int totalPayments = results.stream()
                .mapToInt(row -> ((Number) row[1]).intValue())
                .sum();

        List<PaymentStatusData> statusDistribution = results.stream()
                .map(row -> {
                    String statusStr = (String) row[0];
                    String status = PaymentStatus.valueOf(statusStr).name();
                    Integer count = ((Number) row[1]).intValue();
                    Double percentage = (count.doubleValue() / totalPayments) * 100;
                    return new PaymentStatusData(status, count, percentage);
                })
                .collect(Collectors.toList());

        PaymentStatusMetrics metrics = calculatePaymentStatusMetrics(statusDistribution, totalPayments);

        return new PaymentStatusResponse(statusDistribution, metrics);
    }

    public PaymentAttemptsResponse getPaymentAttemptsByStatus(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = paymentAttemptRepository.findAttemptsByStatus(start, end);

        List<PaymentAttemptData> attempts = results.stream()
                .map(row -> new PaymentAttemptData(
                        ((PaymentAttemptStatus) row[0]).name(),
                        ((Number) row[1]).intValue()
                ))
                .collect(Collectors.toList());

        return new PaymentAttemptsResponse(attempts);
    }

    // Dashboard de Métodos de Pago
    public PaymentMethodResponse getPaymentMethodRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = paymentRepository.findRevenueByPaymentMethod(start, end);

        List<PaymentMethodRevenueData> revenueByMethod = results.stream()
                .map(row -> new PaymentMethodRevenueData(
                        (String) row[0], // description
                        (String) row[1], // gateway
                        ((Number) row[2]).doubleValue(), // revenue
                        ((Number) row[3]).intValue() // transaction count
                ))
                .collect(Collectors.toList());

        PaymentMethodMetrics metrics = calculatePaymentMethodMetrics(revenueByMethod);

        return new PaymentMethodResponse(revenueByMethod, metrics);
    }

    public PaymentMethodDistributionResponse getPaymentMethodDistribution(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = paymentRepository.findPaymentMethodUsage(start, end);

        int totalUsage = results.stream()
                .mapToInt(row -> ((Number) row[1]).intValue())
                .sum();

        List<PaymentMethodDistributionData> distribution = results.stream()
                .map(row -> {
                    String description = (String) row[0];
                    Integer count = ((Number) row[1]).intValue();
                    Double percentage = (count.doubleValue() / totalUsage) * 100;
                    return new PaymentMethodDistributionData(description, count, percentage);
                })
                .collect(Collectors.toList());

        return new PaymentMethodDistributionResponse(distribution);
    }

    // Dashboard de Servicios Más Vendidos
    public TopServicesResponse getTopServices(LocalDate startDate, LocalDate endDate, Integer limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = saleProfileRepository.findTopServices(start, end, limit != null ? limit : 10);

        List<ServiceSalesData> topServices = results.stream()
                .map(row -> new ServiceSalesData(
                        ((Number) row[0]).longValue(), // serviceId
                        (String) row[1], // serviceName
                        ((Number) row[2]).intValue(), // quantitySold
                        ((Number) row[3]).doubleValue() // totalRevenue
                ))
                .collect(Collectors.toList());

        ServiceMetrics metrics = calculateServiceMetrics(topServices);

        return new TopServicesResponse(topServices, metrics);
    }

    public ServiceTrendResponse getServiceTrends(LocalDate startDate, LocalDate endDate, List<Long> serviceIds) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = saleProfileRepository.findServiceTrends(start, end, serviceIds);

        List<ServiceTrendData> trends = results.stream()
                .map(row -> new ServiceTrendData(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        (String) row[1],
                        ((Number) row[2]).intValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .collect(Collectors.toList());

        return new ServiceTrendResponse(trends);
    }

    // Dashboard de Performance por Copypoint
    public CopypointPerformanceResponse getCopypointPerformance(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = saleRepository.findCopypointPerformance(start, end);

        List<CopypointPerformanceData> performance = results.stream()
                .map(row -> new CopypointPerformanceData(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).intValue(),
                        ((Number) row[4]).doubleValue()
                ))
                .collect(Collectors.toList());

        CopypointMetrics metrics = calculateCopypointMetrics(performance);

        return new CopypointPerformanceResponse(performance, metrics);
    }

    public CopypointTrendResponse getCopypointTrends(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = saleRepository.findCopypointTrends(start, end);

        List<CopypointTrendData> trends = results.stream()
                .map(row -> new CopypointTrendData(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue(),
                        (String) row[2],
                        ((Number) row[3]).doubleValue(),
                        ((Number) row[4]).intValue()
                ))
                .collect(Collectors.toList());

        return new CopypointTrendResponse(trends);
    }

    // Métodos auxiliares para cálculos
    private SalesMetrics calculateSalesMetrics(List<SalesTimelineData> timeline) {
        double totalSales = timeline.stream().mapToDouble(SalesTimelineData::totalSales).sum();
        int totalTransactions = timeline.stream().mapToInt(SalesTimelineData::transactionCount).sum();
        double averagePerTransaction = totalTransactions > 0 ? totalSales / totalTransactions : 0;

        return new SalesMetrics(totalSales, averagePerTransaction, totalTransactions);
    }

    private SalesMetrics calculateGlobalMetrics(List<SalesByCopypointData> salesByLocation) {
        double totalSales = salesByLocation.stream().mapToDouble(SalesByCopypointData::totalSales).sum();
        int totalTransactions = salesByLocation.stream().mapToInt(SalesByCopypointData::transactionCount).sum();
        double averagePerTransaction = totalTransactions > 0 ? totalSales / totalTransactions : 0;

        return new SalesMetrics(totalSales, averagePerTransaction, totalTransactions);
    }

    private PaymentStatusMetrics calculatePaymentStatusMetrics(List<PaymentStatusData> statusDistribution, int totalPayments) {
        int successfulPayments = statusDistribution.stream()
                .filter(status -> "COMPLETED".equals(status.status()) || "CAPTURED".equals(status.status()))
                .mapToInt(PaymentStatusData::count)
                .sum();

        int pendingPayments = statusDistribution.stream()
                .filter(status -> "PENDING".equals(status.status()) || "PROCESSING".equals(status.status()))
                .mapToInt(PaymentStatusData::count)
                .sum();

        int failedPayments = statusDistribution.stream()
                .filter(status -> "FAILED".equals(status.status()) || "CANCELLED".equals(status.status()) || "REJECTED".equals(status.status()))
                .mapToInt(PaymentStatusData::count)
                .sum();

        double successRate = totalPayments > 0 ? (successfulPayments * 100.0) / totalPayments : 0;

        return new PaymentStatusMetrics(successRate, pendingPayments, failedPayments, totalPayments);
    }

    private PaymentMethodMetrics calculatePaymentMethodMetrics(List<PaymentMethodRevenueData> revenueByMethod) {
        String mostUsedMethod = revenueByMethod.stream()
                .max((a, b) -> Integer.compare(a.transactionCount(), b.transactionCount()))
                .map(PaymentMethodRevenueData::methodDescription)
                .orElse("");

        Map<String, Double> revenueByGateway = revenueByMethod.stream()
                .collect(Collectors.groupingBy(
                        PaymentMethodRevenueData::gateway,
                        Collectors.summingDouble(PaymentMethodRevenueData::totalRevenue)
                ));

        List<GatewayRevenueData> gatewayRevenues = revenueByGateway.entrySet().stream()
                .map(entry -> new GatewayRevenueData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return new PaymentMethodMetrics(mostUsedMethod, gatewayRevenues);
    }

    private ServiceMetrics calculateServiceMetrics(List<ServiceSalesData> topServices) {
        List<ServiceSalesData> top5Services = topServices.stream()
                .limit(5)
                .collect(Collectors.toList());

        List<ServiceRevenueData> revenueByService = topServices.stream()
                .map(service -> new ServiceRevenueData(service.serviceName(), service.totalRevenue()))
                .collect(Collectors.toList());

        return new ServiceMetrics(top5Services, revenueByService);
    }

    private CopypointMetrics calculateCopypointMetrics(List<CopypointPerformanceData> performance) {
        String mostProfitableCopypoint = performance.stream()
                .max((a, b) -> Double.compare(a.totalSales(), b.totalSales()))
                .map(CopypointPerformanceData::copypointName)
                .orElse("");

        double averageSales = performance.stream()
                .mapToDouble(CopypointPerformanceData::totalSales)
                .average()
                .orElse(0.0);

        return new CopypointMetrics(mostProfitableCopypoint, averageSales);
    }
}
