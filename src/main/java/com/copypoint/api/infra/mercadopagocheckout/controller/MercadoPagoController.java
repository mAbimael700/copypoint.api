package com.copypoint.api.infra.mercadopagocheckout.controller;

import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentResponse;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;
import com.copypoint.api.infra.mercadopagocheckout.service.MercadoPagoService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/mercadopago")
public class MercadoPagoController {
    @Autowired
    private MercadoPagoService mercadoPagoService;

    /**
     * Crear un nuevo pago con MercadoPago
     * @param request Datos del pago a crear
     * @return PaymentResponse con los datos del pago creado
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = mercadoPagoService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            PaymentResponse errorResponse = new PaymentResponse(
                    false,
                    e.getMessage(),
                    null,
                    null,
                    null,
                    null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (MPException | MPApiException e) {
            PaymentResponse errorResponse = new PaymentResponse(
                    false,
                    "Error al crear el pago: " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            PaymentResponse errorResponse = new PaymentResponse(
                    false,
                    "Error interno del servidor: " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Consultar el estado de un pago
     * @param paymentId ID del pago a consultar
     * @return PaymentStatusResponse con el estado del pago
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable @NotNull Long paymentId) {
        try {
            PaymentStatusResponse response = mercadoPagoService.getPaymentStatus(paymentId);

            if (response.errorMessage() != null) {
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            PaymentStatusResponse errorResponse = new PaymentStatusResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Error interno del servidor: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Webhook para recibir notificaciones de MercadoPago
     * @param payload Datos del webhook
     * @return Confirmación de recepción
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Extraer datos del payload de MercadoPago
            String action = (String) payload.get("action");
            String type = (String) payload.get("type");

            // Procesar solo notificaciones de pago
            if ("payment.updated".equals(action) || "payment".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if (data != null && data.get("id") != null) {
                    String paymentId = data.get("id").toString();

                    // Obtener el estado del pago (esto podría requerir una consulta adicional a la API de MP)
                    // Por ahora, pasamos null y el servicio hará la consulta
                    mercadoPagoService.handleWebhook(paymentId, null);
                }
            }

            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error procesando webhook"));
        }
    }

    /**
     * Endpoint alternativo para webhook con parámetros de consulta
     * @param id ID del pago
     * @param topic Tipo de notificación
     * @return Confirmación de recepción
     */
    @PostMapping("/webhook/notification")
    public ResponseEntity<Map<String, String>> handleWebhookNotification(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String topic) {
        try {
            if ("payment".equals(topic) && id != null) {
                mercadoPagoService.handleWebhook(id, null);
            }

            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (Exception e) {
            System.err.println("Error procesando webhook notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error procesando webhook"));
        }
    }

    /**
     * Endpoint para manejar redirecciones de éxito
     * @param paymentId ID del pago
     * @return Estado del pago
     */
    @GetMapping("/success")
    public ResponseEntity<PaymentStatusResponse> handleSuccess(@RequestParam("payment_id") Long paymentId) {
        return getPaymentStatus(paymentId);
    }

    /**
     * Endpoint para manejar redirecciones de fallo
     * @param paymentId ID del pago
     * @return Estado del pago
     */
    @GetMapping("/failure")
    public ResponseEntity<PaymentStatusResponse> handleFailure(@RequestParam("payment_id") Long paymentId) {
        return getPaymentStatus(paymentId);
    }

    /**
     * Endpoint para manejar redirecciones de pendiente
     * @param paymentId ID del pago
     * @return Estado del pago
     */
    @GetMapping("/pending")
    public ResponseEntity<PaymentStatusResponse> handlePending(@RequestParam("payment_id") Long paymentId) {
        return getPaymentStatus(paymentId);
    }

    /**
     * Endpoint de salud para verificar el estado del servicio
     * @return Estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "MercadoPago Payment Service"
        ));
    }
}
