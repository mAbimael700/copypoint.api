package com.copypoint.api.infra.mercadopago.service;

import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * Servicio especializado para consultas de pagos
 * Encapsula la lógica de búsqueda y manejo de errores
 */
@Service
public class PaymentQueryService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentQueryService.class);

    @Autowired
    private PaymentService paymentService;

    public Payment getPaymentByGatewayId(String paymentId) {
        try {
            return paymentService.findByGatewayId(paymentId).orElseThrow(() ->
                    new NoSuchElementException("Payment not found with gateway ID: " + paymentId));

        } catch (NoSuchElementException e) {
            logger.error("Error al consultar el payment desde PaymentService: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error inesperado al consultar payment por gateway ID {}: {}", paymentId, e.getMessage());
            return null;
        }
    }

    public Payment getPaymentById(Long paymentId) {
        try {
            return paymentService.findById(paymentId).orElseThrow(() ->
                    new NoSuchElementException("Payment not found with ID: " + paymentId));

        } catch (NoSuchElementException e) {
            logger.error("Error al consultar el payment por ID {}: {}", paymentId, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error inesperado al consultar payment por ID {}: {}", paymentId, e.getMessage());
            return null;
        }
    }
}
