package com.copypoint.api.domain.paymentattempt.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentAttemptService.class);

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    // Configure ObjectMapper with Java 8 time support
    private final ObjectMapper objectMapper;

    public PaymentAttemptService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Crea un nuevo intento de pago con respuesta de gateway como objeto
     */
    public PaymentAttempt createPaymentAttempt(Payment payment, PaymentAttemptStatus status, Object gatewayResponse) {
        try {
            String jsonResponse = null;
            if (gatewayResponse != null) {
                jsonResponse = objectMapper.writeValueAsString(gatewayResponse);
            }

            return createPaymentAttempt(payment, status, null, jsonResponse);
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar gateway response: {}", e.getMessage());
            // Crear el intento sin la respuesta del gateway si hay error de serialización
            return createPaymentAttempt(payment, status, "JSON_SERIALIZATION_ERROR", null);
        }
    }

    /**
     * Crea un nuevo intento de pago con respuesta de gateway como string JSON
     */
    public PaymentAttempt createPaymentAttempt(Payment payment, PaymentAttemptStatus status, String gatewayResponseJson) {
        return createPaymentAttempt(payment, status, null, gatewayResponseJson);
    }

    /**
     * Crea un nuevo intento de pago con código de error
     */
    public PaymentAttempt createPaymentAttemptWithError(Payment payment, PaymentAttemptStatus status, String errorCode, String errorMessage) {
        return createPaymentAttempt(payment, status, errorCode, errorMessage);
    }

    /**
     * Método principal para crear un intento de pago
     */
    public PaymentAttempt createPaymentAttempt(Payment payment, PaymentAttemptStatus status, String errorCode, String gatewayResponse) {
        try {
            PaymentAttempt attempt = new PaymentAttempt();
            attempt.setPaymentReference(payment);
            attempt.setStatus(status);
            attempt.setErrorCode(errorCode);
            attempt.setCreatedAt(LocalDateTime.now());
            attempt.setModifiedAt(LocalDateTime.now());

            // Validar y procesar la respuesta del gateway
            if (gatewayResponse != null && !gatewayResponse.trim().isEmpty()) {
                String processedResponse = processGatewayResponse(gatewayResponse);
                attempt.setGatewayResponse(processedResponse);
            }

            PaymentAttempt savedAttempt = paymentAttemptRepository.save(attempt);

            logger.info("PaymentAttempt creado - ID: {}, Payment ID: {}, Status: {}",
                    savedAttempt.getId(), payment.getId(), status);

            return savedAttempt;

        } catch (Exception e) {
            logger.error("Error al crear PaymentAttempt para Payment ID {}: {}", payment.getId(), e.getMessage());
            throw new RuntimeException("Error al crear intento de pago", e);
        }
    }

    /**
     * Procesa y valida la respuesta del gateway para asegurar que sea JSON válido
     */
    private String processGatewayResponse(String gatewayResponse) {
        try {
            // Si ya es un JSON válido, devolverlo tal como está
            if (isValidJson(gatewayResponse)) {
                return gatewayResponse;
            }

            // Si no es JSON válido, encapsularlo en un objeto JSON simple
            return objectMapper.writeValueAsString(new Object() {
                public final String message = gatewayResponse;
                public final String timestamp = LocalDateTime.now().toString();
            });

        } catch (JsonProcessingException e) {
            logger.warn("Error al procesar gateway response, usando valor por defecto: {}", e.getMessage());
            return "{\"error\":\"Failed to process gateway response\",\"original\":\"" +
                    gatewayResponse.replace("\"", "\\\"") + "\"}";
        }
    }

    /**
     * Verifica si una cadena es un JSON válido
     */
    private boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Actualiza el estado de un intento de pago
     */
    public PaymentAttempt updatePaymentAttemptStatus(Long attemptId, PaymentAttemptStatus newStatus) {
        Optional<PaymentAttempt> attemptOpt = paymentAttemptRepository.findById(attemptId);

        if (attemptOpt.isEmpty()) {
            throw new IllegalArgumentException("PaymentAttempt no encontrado con ID: " + attemptId);
        }

        PaymentAttempt attempt = attemptOpt.get();
        attempt.setStatus(newStatus);
        attempt.setModifiedAt(LocalDateTime.now());

        return paymentAttemptRepository.save(attempt);
    }

    /**
     * Obtiene todos los intentos de pago para un payment específico
     */
    @Transactional(readOnly = true)
    public List<PaymentAttempt> getPaymentAttemptsByPaymentId(Long paymentId) {
        return paymentAttemptRepository.findByPaymentReferenceIdOrderByCreatedAtDesc(paymentId);
    }

    /**
     * Obtiene el último intento de pago para un payment específico
     */
    @Transactional(readOnly = true)
    public Optional<PaymentAttempt> getLatestPaymentAttempt(Long paymentId) {
        List<PaymentAttempt> attempts = paymentAttemptRepository.findByPaymentReferenceIdOrderByCreatedAtDesc(paymentId);
        return attempts.isEmpty() ? Optional.empty() : Optional.of(attempts.get(0));
    }

    /**
     * Obtiene intentos de pago por estado
     */
    @Transactional(readOnly = true)
    public List<PaymentAttempt> getPaymentAttemptsByStatus(PaymentAttemptStatus status) {
        return paymentAttemptRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Cuenta el número de intentos para un payment específico
     */
    @Transactional(readOnly = true)
    public long countPaymentAttempts(Long paymentId) {
        return paymentAttemptRepository.countByPaymentReferenceId(paymentId);
    }

    /**
     * Elimina intentos de pago antiguos (útil para limpieza)
     */
    public void cleanupOldPaymentAttempts(LocalDateTime beforeDate) {
        try {
            List<PaymentAttempt> oldAttempts = paymentAttemptRepository.findByCreatedAtBefore(beforeDate);
            if (!oldAttempts.isEmpty()) {
                paymentAttemptRepository.deleteAll(oldAttempts);
                logger.info("Eliminados {} intentos de pago antiguos", oldAttempts.size());
            }
        } catch (Exception e) {
            logger.error("Error al limpiar intentos de pago antiguos: {}", e.getMessage());
        }
    }

    /**
     * Crea un intento de pago para una preferencia de MercadoPago
     */
    public PaymentAttempt createMercadoPagoPreferenceAttempt(Payment payment, com.mercadopago.resources.preference.Preference preference) {
        try {
            // Crear objeto simplificado para evitar problemas de serialización
            var simplifiedPreference = new Object() {
                public final String id = preference.getId();
                public final String initPoint = preference.getInitPoint();
                public final String sandboxInitPoint = preference.getSandboxInitPoint();
                public final String dateCreated = preference.getDateCreated() != null ?
                        preference.getDateCreated().toString() : null;
                public final String externalReference = preference.getExternalReference();
                public final String type = "PREFERENCE_CREATION";
            };

            return createPaymentAttempt(payment, PaymentAttemptStatus.PENDING, simplifiedPreference);

        } catch (Exception e) {
            logger.error("Error al crear intento para preferencia MercadoPago: {}", e.getMessage());
            return createPaymentAttemptWithError(payment, PaymentAttemptStatus.FAILED,
                    "PREFERENCE_CREATION_ERROR", e.getMessage());
        }
    }
}
