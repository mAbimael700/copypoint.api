package com.copypoint.api.infra.mercadopagocheckout.service;

import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.infra.mercadopagocheckout.factory.MercadoPagoItemFactory;
import com.copypoint.api.infra.mercadopagocheckout.factory.MercadoPagoPayerFactory;
import com.copypoint.api.infra.mercadopagocheckout.factory.MercadoPagoUrlFactory;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;


@Service
public class MercadoPagoGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoGatewayService.class);

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigService;

    @Autowired
    private MercadoPagoGatewayConfigurationService mercadoPagoGatewayConfigurationService;

    @Autowired
    private MercadoPagoItemFactory itemFactory;

    @Autowired
    private MercadoPagoPayerFactory payerFactory;

    @Autowired
    private MercadoPagoUrlFactory urlFactory;

    public Preference createPreference(Payment payment, PaymentRequest request) throws MPException, MPApiException {
        Sale sale = payment.getSale();

        // Configurar MercadoPago SDK con el token del store/copypoint
        boolean configured = mercadoPagoGatewayConfigurationService.configureForSale(sale);
        if (!configured) {
            throw new IllegalStateException("No se encontró configuración de MercadoPago para la venta");
        }

        PreferenceClient client = new PreferenceClient();

        // Crear componentes de la preferencia usando las factories
        List<PreferenceItemRequest> items = itemFactory.createItemsFromSale(sale, request);
        PreferencePayerRequest payer = payerFactory.createPayerRequest(request);
        PreferenceBackUrlsRequest backUrls = urlFactory.createBackUrlsRequest(payment);

        // Obtener email del vendedor desde la configuración
        String vendorEmail = mercadoPagoConfigService.getVendorEmailForSale(sale);

        // Crear la preferencia
        PreferenceRequest preferenceRequest = buildPreferenceRequest(
                items, payer, backUrls, payment, request, sale, vendorEmail);

        logger.info("Creando preferencia para Copypoint: {}, Vendor: {}",
                sale.getCopypoint().getId(), vendorEmail);

        return createPreferenceWithErrorHandling(client, preferenceRequest);
    }

    private PreferenceRequest buildPreferenceRequest(
            List<PreferenceItemRequest> items,
            PreferencePayerRequest payer,
            PreferenceBackUrlsRequest backUrls,
            Payment payment,
            PaymentRequest request,
            Sale sale,
            String vendorEmail) {

        return PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(payment.getId().toString())
                .statementDescriptor(request.description() != null ? request.description() : "Pago CopyPoint")
                .metadata(createMetadata(sale, vendorEmail, payment.getId(), request.amount()))
                .build();
    }

    private Map<String, Object> createMetadata(Sale sale, String vendorEmail, Long paymentId, Double amount) {
        return Map.of(
                "store_id", sale.getCopypoint().getStore().getId().toString(),
                "copypoint_id", sale.getCopypoint().getId().toString(),
                "vendor_email", vendorEmail,
                "sale_id", sale.getId().toString(),
                "payment_amount", amount.toString()
        );
    }

    private Preference createPreferenceWithErrorHandling(PreferenceClient client, PreferenceRequest preferenceRequest)
            throws MPException, MPApiException {
        try {
            Preference preference = client.create(preferenceRequest);
            logger.info("Preference creada exitosamente - ID: {}", preference.getId());
            return preference;
        } catch (MPApiException e) {
            handleMPApiException(e);
            throw e; // Re-lanzar después del logging
        } catch (MPException e) {
            logger.error("MPException: {}", e.getMessage());
            throw e;
        }
    }

    private void handleMPApiException(MPApiException e) throws MPApiException {
        logger.error("MPApiException details:");
        logger.error("Status Code: {}", e.getStatusCode());
        logger.error("Message: {}", e.getMessage());

        if (e.getApiResponse() != null) {
            logger.error("API Response: {}", e.getApiResponse().getContent());
        }

        // Crear nueva excepción con información adicional
        throw new MPApiException(
                String.format("MercadoPago API Error - Status: %d, Message: %s%s",
                        e.getStatusCode(),
                        e.getMessage(),
                        e.getApiResponse() != null ? ", Response: " + e.getApiResponse().getContent() : ""),
                e.getApiResponse());
    }

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