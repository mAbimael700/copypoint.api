package com.copypoint.api.infra.mercadopagocheckout.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoGatewayService.class);

    @Value("${frontend.success.url}")
    private String successUrl;

    @Value("${frontend.failure.url}")
    private String failureUrl;

    @Value("${frontend.pending.url}")
    private String pendingUrl;

    public Preference createPreference(Payment payment, PaymentRequest request) throws MPException, MPApiException {
        PreferenceClient client = new PreferenceClient();

        Sale sale = payment.getSale();

        // Crear items desde los saleProfiles
        List<PreferenceItemRequest> items = createItemsFromSale(sale);

        // Configurar payer
        PreferencePayerRequest payer = createPayerRequest(request);

        // Configurar URLs de retorno
        PreferenceBackUrlsRequest backUrls = createBackUrlsRequest(payment);

        // Crear la preferencia
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                //.autoReturn("approved")
                .externalReference(payment.getId().toString())
                .statementDescriptor(request.description() != null ? request.description() : "Pago CopyPoint")
                .build();

        logger.info("Enviando request a MercadoPago...");

        try {
            Preference preference = client.create(preferenceRequest);
            logger.info("Preference creada exitosamente - ID: {}", preference.getId());
            return preference;
        } catch (MPApiException e) {
            // Obtener detalles del error
            logger.error("MPApiException details:");
            logger.error("Status Code: {}", e.getStatusCode());
            logger.error("Message: {}", e.getMessage());

            if (e.getApiResponse() != null) {
                logger.error("API Response: {}", e.getApiResponse().getContent());
            }

            // Re-lanzar con información adicional
            throw new MPApiException(
                    "MercadoPago API Error - Status: " + e.getStatusCode() +
                    ", Message: " + e.getMessage() +
                    (e.getApiResponse() != null ? ", Response: "
                            + e.getApiResponse().getContent() : ""), e.getApiResponse());

        } catch (MPException e) {
            logger.error("MPException: {}", e.getMessage());
            throw e;
        }
    }

    private List<PreferenceItemRequest> createItemsFromSale(Sale sale) {
        List<PreferenceItemRequest> items = new ArrayList<>();

        for (SaleProfile saleProfile : sale.getSaleProfiles()) {
            // Validar datos antes de crear el item
            String title = saleProfile.getService().getName();
            String description = saleProfile.getProfile().getDescription();
            Integer quantity = saleProfile.getQuantity();
            BigDecimal unitPrice = BigDecimal.valueOf(saleProfile.getUnitPrice());
            String currencyId = sale.getCurrency();

            // Log para debugging
            logger.debug("Creating item: title={}, quantity={}, unitPrice={}, currency={}",
                    title, quantity, unitPrice, currencyId);

            // Validaciones
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
            }
            if (currencyId == null || currencyId.trim().isEmpty()) {
                throw new IllegalArgumentException("La moneda no puede estar vacía");
            }

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(title.trim())
                    .description(description != null ? description.trim() : title.trim())
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .currencyId(currencyId.toUpperCase())
                    .build();
            items.add(itemRequest);
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un item");
        }

        return items;
    }

    private PreferencePayerRequest createPayerRequest(PaymentRequest request) {
        // Validar datos del payer
        if (request.payer() == null) {
            throw new IllegalArgumentException("Los datos del pagador son requeridos");
        }

        String firstName = request.payer().firstName();
        String lastName = request.payer().lastName();
        String email = request.payer().email();
        String phone = request.payer().phone();
        String identificationType = request.payer().identificationType();
        String identification = request.payer().identification();

        // Validaciones básicas
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del pagador es requerido");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del pagador es requerido");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del pagador es requerido");
        }

        logger.debug("Creating payer: name={} {}, email={}", firstName, lastName, email);

        return PreferencePayerRequest.builder()
                .name(firstName.trim())
                .surname(lastName.trim())
                .email(email.trim())
                .phone(phone != null ? PhoneRequest.builder()
                        .number(phone.trim())
                        .build() : null)
                .identification(identificationType != null && identification != null ?
                        IdentificationRequest.builder()
                                .type(identificationType.trim())
                                .number(identification.trim())
                                .build() : null)
                .build();
    }

    private PreferenceBackUrlsRequest createBackUrlsRequest(Payment payment) {
        logger.info("Configurando URLs: success={}, failure={}, pending={}",
                successUrl, failureUrl, pendingUrl);

        return PreferenceBackUrlsRequest.builder()
                .success(successUrl + "?payment_id=" + payment.getId())
                .failure(failureUrl + "?payment_id=" + payment.getId())
                .pending(pendingUrl + "?payment_id=" + payment.getId())
                .build();
    }

    // Resto de métodos sin cambios...
    public PaymentStatus getPaymentStatusFromGateway(String gatewayId) {
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(gatewayId));

            return mapMercadoPagoStatus(mpPayment.getStatus());
        } catch (Exception e) {
            logger.error("Error al consultar estado desde MercadoPago: {}", e.getMessage());
            return null;
        }
    }

    public PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }
}