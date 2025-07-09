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
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoGatewayService {

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
                .autoReturn("approved")
                .externalReference(payment.getId().toString())
                .statementDescriptor(request.description() != null ? request.description() : "Pago CopyPoint")
                .build();

        return client.create(preferenceRequest);
    }

    public PaymentStatus getPaymentStatusFromGateway(String gatewayId) {
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(gatewayId));

            return mapMercadoPagoStatus(mpPayment.getStatus());
        } catch (Exception e) {
            // Log del error pero no fallar
            System.err.println("Error al consultar estado desde MercadoPago: " + e.getMessage());
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

    private List<PreferenceItemRequest> createItemsFromSale(Sale sale) {
        List<PreferenceItemRequest> items = new ArrayList<>();

        for (SaleProfile saleProfile : sale.getSaleProfiles()) {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(saleProfile.getService().getName()) // Asumiendo que Service tiene un campo name
                    .description(saleProfile.getProfile().getDescription()) // Asumiendo que Profile tiene description
                    .quantity(saleProfile.getQuantity())
                    .unitPrice(BigDecimal.valueOf(saleProfile.getUnitPrice()))
                    .currencyId(sale.getCurrency())
                    .build();
            items.add(itemRequest);
        }

        return items;
    }

    private PreferencePayerRequest createPayerRequest(PaymentRequest request) {
        return PreferencePayerRequest.builder()
                .name(request.payer().firstName())
                .surname(request.payer().lastName())
                .email(request.payer().email())
                .phone(PhoneRequest.builder()
                        .number(request.payer().phone())
                        .build())
                .identification(IdentificationRequest.builder()
                        .type(request.payer().identificationType())
                        .number(request.payer().identification())
                        .build())
                .build();
    }

    private PreferenceBackUrlsRequest createBackUrlsRequest(Payment payment) {
        return PreferenceBackUrlsRequest.builder()
                .success(successUrl + "?payment_id=" + payment.getId())
                .failure(failureUrl + "?payment_id=" + payment.getId())
                .pending(pendingUrl + "?payment_id=" + payment.getId())
                .build();
    }
}
