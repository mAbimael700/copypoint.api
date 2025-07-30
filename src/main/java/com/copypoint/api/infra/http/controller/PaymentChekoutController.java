package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.gatewaycheckout.dto.CheckoutData;
import com.copypoint.api.domain.gatewaycheckout.parser.dto.ParserStats;
import com.copypoint.api.domain.gatewaycheckout.parser.service.ParserHealthService;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.service.PaymentAttemptQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentChekoutController {

    @Autowired
    private PaymentAttemptQueryService paymentAttemptQueryService;

    @Autowired
    private ParserHealthService parserHealthService;

    /**
     * Obtiene la URL de checkout para un payment
     * GET /api/payments/{paymentId}/checkout-url
     */
    @GetMapping("/{paymentId}/checkout-url")
    public ResponseEntity<Map<String, String>> getCheckoutUrl(@PathVariable Long paymentId) {
        Optional<String> checkoutUrl = paymentAttemptQueryService.getLatestCheckoutUrl(paymentId);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = Map.of(
                "checkoutUrl", checkoutUrl.get(),
                "paymentId", paymentId.toString()
        );

        return ResponseEntity.ok(response);
    }
    /**
     * Obtiene los datos de checkout del último intento (independiente del estado)
     * GET /api/payments/{paymentId}/checkout-data
     */
    @GetMapping("/{paymentId}/checkout-data")
    public ResponseEntity<CheckoutData> getCheckoutData(@PathVariable Long paymentId) {
        Optional<CheckoutData> checkoutData = paymentAttemptQueryService.getLatestCheckoutData(paymentId);

        if (checkoutData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(checkoutData.get());
    }

    /**
     * Redirige directamente al checkout del último intento
     * GET /api/payments/{paymentId}/checkout
     */
    @GetMapping("/{paymentId}/checkout")
    public ResponseEntity<Void> redirectToCheckout(@PathVariable Long paymentId) {
        Optional<String> checkoutUrl = paymentAttemptQueryService.getLatestCheckoutUrl(paymentId);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", checkoutUrl.get());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // === ENDPOINTS POR ESTADO ===

    /**
     * Obtiene la URL de checkout del último intento exitoso
     * GET /api/payments/{paymentId}/checkout-url/successful
     */
    @GetMapping("/{paymentId}/checkout-url/successful")
    public ResponseEntity<Map<String, String>> getSuccessfulCheckoutUrl(@PathVariable Long paymentId) {
        Optional<String> checkoutUrl = paymentAttemptQueryService.getLatestSuccessfulCheckoutUrl(paymentId);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = Map.of(
                "checkoutUrl", checkoutUrl.get(),
                "paymentId", paymentId.toString(),
                "type", "successful"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la URL de checkout del último intento activo/pendiente
     * GET /api/payments/{paymentId}/checkout-url/active
     */
    @GetMapping("/{paymentId}/checkout-url/active")
    public ResponseEntity<Map<String, String>> getActiveCheckoutUrl(@PathVariable Long paymentId) {
        Optional<String> checkoutUrl = paymentAttemptQueryService.getLatestActiveCheckoutUrl(paymentId);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = Map.of(
                "checkoutUrl", checkoutUrl.get(),
                "paymentId", paymentId.toString(),
                "type", "active"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la URL de checkout por estado específico
     * GET /api/payments/{paymentId}/checkout-url/status/{status}
     */
    @GetMapping("/{paymentId}/checkout-url/status/{status}")
    public ResponseEntity<Map<String, String>> getCheckoutUrlByStatus(
            @PathVariable Long paymentId,
            @PathVariable PaymentAttemptStatus status) {

        Optional<String> checkoutUrl = paymentAttemptQueryService.getCheckoutUrlByStatus(paymentId, status);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = Map.of(
                "checkoutUrl", checkoutUrl.get(),
                "paymentId", paymentId.toString(),
                "status", status.toString()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los datos de checkout del último intento exitoso
     * GET /api/payments/{paymentId}/checkout-data/successful
     */
    @GetMapping("/{paymentId}/checkout-data/successful")
    public ResponseEntity<CheckoutData> getSuccessfulCheckoutData(@PathVariable Long paymentId) {
        Optional<CheckoutData> checkoutData = paymentAttemptQueryService.getLatestSuccessfulCheckoutData(paymentId);

        if (checkoutData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(checkoutData.get());
    }

    /**
     * Obtiene los datos de checkout del último intento activo
     * GET /api/payments/{paymentId}/checkout-data/active
     */
    @GetMapping("/{paymentId}/checkout-data/active")
    public ResponseEntity<CheckoutData> getActiveCheckoutData(@PathVariable Long paymentId) {
        Optional<CheckoutData> checkoutData = paymentAttemptQueryService.getLatestActiveCheckoutData(paymentId);

        if (checkoutData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(checkoutData.get());
    }

    /**
     * Obtiene los datos de checkout por estado específico
     * GET /api/payments/{paymentId}/checkout-data/status/{status}
     */
    @GetMapping("/{paymentId}/checkout-data/status/{status}")
    public ResponseEntity<CheckoutData> getCheckoutDataByStatus(
            @PathVariable Long paymentId,
            @PathVariable PaymentAttemptStatus status) {

        Optional<CheckoutData> checkoutData = paymentAttemptQueryService.getLatestCheckoutDataByStatus(paymentId, status);

        if (checkoutData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(checkoutData.get());
    }

    // === ENDPOINTS DE TODOS LOS INTENTOS ===

    /**
     * Obtiene todos los datos de checkout para un payment
     * GET /api/payments/{paymentId}/checkout-data/all
     */
    @GetMapping("/{paymentId}/checkout-data/all")
    public ResponseEntity<List<CheckoutData>> getAllCheckoutData(@PathVariable Long paymentId) {
        List<CheckoutData> allCheckoutData = paymentAttemptQueryService.getAllCheckoutDataForPayment(paymentId);
        return ResponseEntity.ok(allCheckoutData);
    }

    /**
     * Obtiene todos los datos de checkout exitosos para un payment
     * GET /api/payments/{paymentId}/checkout-data/all/successful
     */
    @GetMapping("/{paymentId}/checkout-data/all/successful")
    public ResponseEntity<List<CheckoutData>> getAllSuccessfulCheckoutData(@PathVariable Long paymentId) {
        List<CheckoutData> successfulCheckoutData = paymentAttemptQueryService.getAllSuccessfulCheckoutDataForPayment(paymentId);
        return ResponseEntity.ok(successfulCheckoutData);
    }

    /**
     * Obtiene todos los datos de checkout activos para un payment
     * GET /api/payments/{paymentId}/checkout-data/all/active
     */
    @GetMapping("/{paymentId}/checkout-data/all/active")
    public ResponseEntity<List<CheckoutData>> getAllActiveCheckoutData(@PathVariable Long paymentId) {
        List<CheckoutData> activeCheckoutData = paymentAttemptQueryService.getAllActiveCheckoutDataForPayment(paymentId);
        return ResponseEntity.ok(activeCheckoutData);
    }

    /**
     * Obtiene todos los datos de checkout fallidos para un payment
     * GET /api/payments/{paymentId}/checkout-data/all/failed
     */
    @GetMapping("/{paymentId}/checkout-data/all/failed")
    public ResponseEntity<List<CheckoutData>> getAllFailedCheckoutData(@PathVariable Long paymentId) {
        List<CheckoutData> failedCheckoutData = paymentAttemptQueryService.getAllFailedCheckoutDataForPayment(paymentId);
        return ResponseEntity.ok(failedCheckoutData);
    }

    // === ENDPOINTS DE VERIFICACIÓN ===

    /**
     * Verifica si un payment tiene checkout disponible (último intento)
     * GET /api/payments/{paymentId}/has-checkout
     */
    @GetMapping("/{paymentId}/has-checkout")
    public ResponseEntity<Map<String, Object>> hasCheckout(@PathVariable Long paymentId) {
        boolean hasLatest = paymentAttemptQueryService.hasLatestCheckoutUrl(paymentId);
        boolean hasSuccessful = paymentAttemptQueryService.hasSuccessfulCheckoutUrl(paymentId);
        boolean hasActive = paymentAttemptQueryService.hasActiveCheckoutUrl(paymentId);

        Map<String, Object> response = Map.of(
                "hasCheckout", hasLatest,
                "hasSuccessful", hasSuccessful,
                "hasActive", hasActive,
                "paymentId", paymentId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica si un payment tiene checkout con estado específico
     * GET /api/payments/{paymentId}/has-checkout/status/{status}
     */
    @GetMapping("/{paymentId}/has-checkout/status/{status}")
    public ResponseEntity<Map<String, Object>> hasCheckoutWithStatus(
            @PathVariable Long paymentId,
            @PathVariable PaymentAttemptStatus status) {

        boolean hasCheckout = paymentAttemptQueryService.hasCheckoutUrlWithStatus(paymentId, status);

        Map<String, Object> response = Map.of(
                "hasCheckout", hasCheckout,
                "status", status.toString(),
                "paymentId", paymentId
        );

        return ResponseEntity.ok(response);
    }

    // === ENDPOINTS PARA REDIRECCIONES ESPECÍFICAS ===

    /**
     * Redirige al checkout del último intento exitoso
     * GET /api/payments/{paymentId}/checkout/successful
     */
    @GetMapping("/{paymentId}/checkout/successful")
    public ResponseEntity<Void> redirectToSuccessfulCheckout(@PathVariable Long paymentId) {
        Optional<String> checkoutUrl = paymentAttemptQueryService.getLatestSuccessfulCheckoutUrl(paymentId);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", checkoutUrl.get());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Redirige al checkout del último intento activo
     * GET /api/payments/{paymentId}/checkout/active
     */
    @GetMapping("/{paymentId}/checkout/active")
    public ResponseEntity<Void> redirectToActiveCheckout(@PathVariable Long paymentId) {
        Optional<String> checkoutUrl = paymentAttemptQueryService.getLatestActiveCheckoutUrl(paymentId);

        if (checkoutUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", checkoutUrl.get());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // === ENDPOINTS DE MONITOREO (sin cambios) ===

    /**
     * Estadísticas de parsers (para monitoring)
     * GET /api/payments/admin/parser-stats?days=7
     */
    @GetMapping("/admin/parser-stats")
    public ResponseEntity<List<ParserStats>> getParserStats(
            @RequestParam(defaultValue = "7") int days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<ParserStats> stats = parserHealthService.getParserStatistics(since);
        return ResponseEntity.ok(stats);
    }

    /**
     * Salud general del sistema de parsing
     * GET /api/payments/admin/parser-health?days=1
     */
    @GetMapping("/admin/parser-health")
    public ResponseEntity<Map<String, Object>> getParserHealth(
            @RequestParam(defaultValue = "1") int days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> health = parserHealthService.getOverallHealth(since);
        return ResponseEntity.ok(health);
    }

    /**
     * PaymentAttempts que no se pueden parsear (para debugging)
     * GET /api/payments/admin/unparseable?days=1&limit=20
     */
    @GetMapping("/admin/unparseable")
    public ResponseEntity<List<Map<String, ? extends Serializable>>> getUnparseableAttempts(
            @RequestParam(defaultValue = "1") int days,
            @RequestParam(defaultValue = "20") int limit) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<PaymentAttempt> unparseable = parserHealthService.findUnparseableAttempts(since, limit);

        List<Map<String, ? extends Serializable>> response = unparseable.stream()
                .map(attempt -> Map.of(
                        "id", attempt.getId(),
                        "paymentId", attempt.getPaymentReference().getId(),
                        "status", attempt.getStatus().toString(),
                        "createdAt", attempt.getCreatedAt().toString(),
                        "gatewayResponse", attempt.getGatewayResponse() != null ?
                                attempt.getGatewayResponse().substring(0, Math.min(200, attempt.getGatewayResponse().length())) + "..." :
                                "null"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
