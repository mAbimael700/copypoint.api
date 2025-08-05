package com.copypoint.api.domain.paymentattempt.service;

import com.copypoint.api.domain.gatewaycheckout.dto.CheckoutData;
import com.copypoint.api.domain.gatewaycheckout.parser.service.PaymentAttemptParserService;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaymentAttemptQueryService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentAttemptQueryService.class);

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private PaymentAttemptParserService parserService;


    // Estados considerados exitosos
    private static final Set<PaymentAttemptStatus> SUCCESS_STATUSES = Set.of(
            PaymentAttemptStatus.SUCCEEDED,
            PaymentAttemptStatus.COMPLETED
    );

    // Estados considerados activos/pendientes
    private static final Set<PaymentAttemptStatus> ACTIVE_STATUSES = Set.of(
            PaymentAttemptStatus.INITIATED,
            PaymentAttemptStatus.PENDING,
            PaymentAttemptStatus.PROCESSING,
            PaymentAttemptStatus.AWAITING_CONFIRMATION,
            PaymentAttemptStatus.REQUIRES_ACTION
    );

    // Estados considerados fallidos
    private static final Set<PaymentAttemptStatus> FAILED_STATUSES = Set.of(
            PaymentAttemptStatus.FAILED,
            PaymentAttemptStatus.DECLINED,
            PaymentAttemptStatus.CANCELLED,
            PaymentAttemptStatus.ABANDONED,
            PaymentAttemptStatus.EXPIRED,
            PaymentAttemptStatus.INSUFFICIENT_FUNDS,
            PaymentAttemptStatus.INVALID_CARD,
            PaymentAttemptStatus.AUTHENTICATION_FAILED,
            PaymentAttemptStatus.FRAUD_DETECTED,
            PaymentAttemptStatus.BLOCKED_BY_GATEWAY,
            PaymentAttemptStatus.NETWORK_ERROR
    );

    /**
     * Obtiene el checkout data del último intento de pago, independiente del estado
     */
    public Optional<CheckoutData> getLatestCheckoutData(Long paymentId) {
        try {
            List<PaymentAttempt> attempts = paymentAttemptRepository
                    .findByPaymentReferenceIdOrderByCreatedAtDesc(paymentId);

            // Buscar el primer intento que sea parseable
            for (PaymentAttempt attempt : attempts) {
                Optional<CheckoutData> checkoutData = parserService.parseGatewayResponse(attempt);
                if (checkoutData.isPresent()) {
                    logger.debug("Checkout data encontrado en intento: {} para Payment ID: {}",
                            attempt.getId(), paymentId);
                    return checkoutData;
                }
            }

            logger.info("No se encontró checkout data parseable para Payment ID: {}", paymentId);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error obteniendo último checkout data para Payment ID: {}", paymentId, e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene el checkout data del último intento exitoso
     */
    public Optional<CheckoutData> getLatestSuccessfulCheckoutData(Long paymentId) {
        return getLatestCheckoutDataByStatuses(paymentId, SUCCESS_STATUSES, "exitoso");
    }

    /**
     * Obtiene el checkout data del último intento activo/pendiente
     */
    public Optional<CheckoutData> getLatestActiveCheckoutData(Long paymentId) {
        return getLatestCheckoutDataByStatuses(paymentId, ACTIVE_STATUSES, "activo");
    }

    /**
     * Obtiene el checkout data del último intento fallido
     */
    public Optional<CheckoutData> getLatestFailedCheckoutData(Long paymentId) {
        return getLatestCheckoutDataByStatuses(paymentId, FAILED_STATUSES, "fallido");
    }

    /**
     * Obtiene el checkout data del último intento con estado específico
     */
    public Optional<CheckoutData> getLatestCheckoutDataByStatus(Long paymentId, PaymentAttemptStatus status) {
        try {
            Optional<PaymentAttempt> attempt = paymentAttemptRepository
                    .findTopByPaymentReferenceIdAndStatusOrderByCreatedAtDesc(paymentId, status);

            if (attempt.isEmpty()) {
                logger.info("No se encontró intento con estado {} para Payment ID: {}", status, paymentId);
                return Optional.empty();
            }

            return parserService.parseGatewayResponse(attempt.get());

        } catch (Exception e) {
            logger.error("Error obteniendo checkout data con estado {} para Payment ID: {}", status, paymentId, e);
            return Optional.empty();
        }
    }

    /**
     * URLs de checkout por diferentes criterios
     */
    public Optional<String> getLatestCheckoutUrl(Long paymentId) {
        return getLatestCheckoutData(paymentId)
                .map(CheckoutData::checkoutUrl);
    }

    public Optional<String> getLatestSuccessfulCheckoutUrl(Long paymentId) {
        return getLatestSuccessfulCheckoutData(paymentId)
                .map(CheckoutData::checkoutUrl);
    }

    public Optional<String> getLatestActiveCheckoutUrl(Long paymentId) {
        return getLatestActiveCheckoutData(paymentId)
                .map(CheckoutData::checkoutUrl);
    }

    public Optional<String> getCheckoutUrlByStatus(Long paymentId, PaymentAttemptStatus status) {
        return getLatestCheckoutDataByStatus(paymentId, status)
                .map(CheckoutData::checkoutUrl);
    }

    /**
     * Obtiene todos los checkout data para un payment, filtrados por estados
     */
    public List<CheckoutData> getAllCheckoutDataForPayment(Long paymentId) {
        return getAllCheckoutDataForPaymentByStatuses(paymentId, null);
    }

    public List<CheckoutData> getAllSuccessfulCheckoutDataForPayment(Long paymentId) {
        return getAllCheckoutDataForPaymentByStatuses(paymentId, SUCCESS_STATUSES);
    }

    public List<CheckoutData> getAllActiveCheckoutDataForPayment(Long paymentId) {
        return getAllCheckoutDataForPaymentByStatuses(paymentId, ACTIVE_STATUSES);
    }

    public List<CheckoutData> getAllFailedCheckoutDataForPayment(Long paymentId) {
        return getAllCheckoutDataForPaymentByStatuses(paymentId, FAILED_STATUSES);
    }

    /**
     * Métodos de verificación
     */
    public boolean hasLatestCheckoutUrl(Long paymentId) {
        return getLatestCheckoutUrl(paymentId).isPresent();
    }

    public boolean hasSuccessfulCheckoutUrl(Long paymentId) {
        return getLatestSuccessfulCheckoutUrl(paymentId).isPresent();
    }

    public boolean hasActiveCheckoutUrl(Long paymentId) {
        return getLatestActiveCheckoutUrl(paymentId).isPresent();
    }

    public boolean hasCheckoutUrlWithStatus(Long paymentId, PaymentAttemptStatus status) {
        return getCheckoutUrlByStatus(paymentId, status).isPresent();
    }

    /**
     * Método utilitario para obtener checkout data por conjunto de estados
     */
    private Optional<CheckoutData> getLatestCheckoutDataByStatuses(Long paymentId,
                                                                   Set<PaymentAttemptStatus> statuses,
                                                                   String statusDescription) {
        try {
            List<PaymentAttempt> attempts = paymentAttemptRepository
                    .findByPaymentReferenceIdOrderByCreatedAtDesc(paymentId);

            Optional<PaymentAttempt> matchingAttempt = attempts.stream()
                    .filter(attempt -> statuses.contains(attempt.getStatus()))
                    .findFirst();

            if (matchingAttempt.isEmpty()) {
                logger.info("No se encontró intento {} para Payment ID: {}", statusDescription, paymentId);
                return Optional.empty();
            }

            logger.debug("Intento {} encontrado con estado: {} para Payment ID: {}",
                    statusDescription, matchingAttempt.get().getStatus(), paymentId);

            return parserService.parseGatewayResponse(matchingAttempt.get());

        } catch (Exception e) {
            logger.error("Error obteniendo checkout data {} para Payment ID: {}", statusDescription, paymentId, e);
            return Optional.empty();
        }
    }

    /**
     * Método utilitario para obtener todos los checkout data filtrados por estados
     */
    private List<CheckoutData> getAllCheckoutDataForPaymentByStatuses(Long paymentId,
                                                                      Set<PaymentAttemptStatus> statuses) {
        try {
            List<PaymentAttempt> attempts = paymentAttemptRepository
                    .findByPaymentReferenceIdOrderByCreatedAtDesc(paymentId);

            return attempts.stream()
                    .filter(attempt -> statuses == null || statuses.contains(attempt.getStatus()))
                    .map(parserService::parseGatewayResponse)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error obteniendo todos los checkout data para Payment ID: {}", paymentId, e);
            return List.of();
        }
    }
}
