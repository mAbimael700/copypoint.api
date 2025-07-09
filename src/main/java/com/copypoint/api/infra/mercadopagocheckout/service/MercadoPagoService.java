package com.copypoint.api.infra.mercadopagocheckout.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentResponse;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;
import com.copypoint.api.domain.payment.repository.PaymentRepository;
import com.copypoint.api.domain.paymentattempt.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MercadoPagoService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Value("${frontend.success.url}")
    private String successUrl;

    @Value("${frontend.failure.url}")
    private String failureUrl;

    @Value("${frontend.pending.url}")
    private String pendingUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            // Verificar que la venta existe
            Optional<Sale> saleOpt = saleRepository.findById(request.saleId());
            if (saleOpt.isEmpty()) {
                return new PaymentResponse(false, "Venta no encontrada", null, null, null, null);
            }

            Sale sale = saleOpt.get();

            // Verificar que la venta tenga items
            if (sale.getSaleProfiles().isEmpty()) {
                return new PaymentResponse(false, "La venta no tiene items asociados", null, null, null, null);
            }

            // Crear el pago en la base de datos
            Payment payment = new Payment();
            payment.setSale(sale);
            payment.setAmount(sale.getTotal());
            payment.setCurrency(sale.getCurrency());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreatedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            // Crear la preferencia en MercadoPago
            PreferenceClient client = new PreferenceClient();

            // Crear items desde los saleProfiles
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

            // Configurar payer
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
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

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl + "?payment_id=" + payment.getId())
                    .failure(failureUrl + "?payment_id=" + payment.getId())
                    .pending(pendingUrl + "?payment_id=" + payment.getId())
                    .build();

            // Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(payment.getId().toString())
                    .statementDescriptor(request.description() != null ? request.description() : "Pago CopyPoint")
                    .build();

            Preference preference = client.create(preferenceRequest);

            // Actualizar el payment con el ID de MercadoPago
            payment.setGatewayId(preference.getId());
            payment.setModifiedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Crear registro de intento de pago
            PaymentAttempt attempt = new PaymentAttempt();
            attempt.setPaymentReference(payment);
            attempt.setStatus(PaymentAttemptStatus.PENDING);
            attempt.setGatewayResponse(objectMapper.writeValueAsString(preference));
            attempt.setCreatedAt(LocalDateTime.now());
            paymentAttemptRepository.save(attempt);

            return new PaymentResponse(
                    true,
                    "Pago creado exitosamente",
                    preference.getInitPoint(),
                    preference.getId(),
                    payment.getId().toString(),
                    PaymentStatus.PENDING
            );

        } catch (MPException | MPApiException e) {
            return new PaymentResponse(false, "Error al crear el pago: " + e.getMessage(), null, null, null, null);
        } catch (Exception e) {
            return new PaymentResponse(false, "Error interno: " + e.getMessage(), null, null, null, null);
        }
    }

    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return new PaymentStatusResponse(null, null, null, null, null, "Pago no encontrado");
            }

            Payment payment = paymentOpt.get();

            // Si tiene gatewayId, consultar el estado en MercadoPago
            if (payment.getGatewayId() != null) {
                updatePaymentStatusFromGateway(payment);
            }

            return new PaymentStatusResponse(
                    payment.getId().toString(),
                    payment.getStatus(),
                    null,
                    payment.getAmount(),
                    payment.getCurrency(),
                    null
            );

        } catch (Exception e) {
            return new PaymentStatusResponse(null, null, null, null, null, "Error al consultar el estado: " + e.getMessage());
        }
    }

    private void updatePaymentStatusFromGateway(Payment payment) {
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(payment.getGatewayId()));

            // Actualizar estado según la respuesta de MercadoPago
            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());
            if (newStatus != payment.getStatus()) {
                payment.setStatus(newStatus);
                payment.setModifiedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }

        } catch (Exception e) {
            // Log del error pero no fallar
            System.err.println("Error al actualizar estado desde MercadoPago: " + e.getMessage());
        }
    }

    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }

    public void handleWebhook(String paymentId, String status) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByGatewayId(paymentId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                PaymentStatus newStatus = mapMercadoPagoStatus(status);

                if (newStatus != payment.getStatus()) {
                    payment.setStatus(newStatus);
                    payment.setModifiedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    // Crear registro de intento
                    PaymentAttempt attempt = new PaymentAttempt();
                    attempt.setPaymentReference(payment);
                    attempt.setStatus(PaymentAttemptStatus.valueOf(newStatus.name()));
                    attempt.setGatewayResponse("{\"status\":\"" + status + "\"}");
                    attempt.setCreatedAt(LocalDateTime.now());
                    paymentAttemptRepository.save(attempt);
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
        }
    }
}
