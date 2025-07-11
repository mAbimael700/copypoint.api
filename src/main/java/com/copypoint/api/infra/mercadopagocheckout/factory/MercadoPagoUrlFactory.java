package com.copypoint.api.infra.mercadopagocheckout.factory;

import com.copypoint.api.domain.payment.Payment;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoUrlFactory {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoUrlFactory.class);

    @Value("${frontend.success.url}")
    private String successUrl;

    @Value("${frontend.failure.url}")
    private String failureUrl;

    @Value("${frontend.pending.url}")
    private String pendingUrl;

    public PreferenceBackUrlsRequest createBackUrlsRequest(Payment payment) {
        validatePayment(payment);

        logger.info("Configurando URLs: success={}, failure={}, pending={}",
                successUrl, failureUrl, pendingUrl);

        return PreferenceBackUrlsRequest.builder()
                .success(buildUrlWithPaymentId(successUrl, payment.getId()))
                .failure(buildUrlWithPaymentId(failureUrl, payment.getId()))
                .pending(buildUrlWithPaymentId(pendingUrl, payment.getId()))
                .build();
    }

    private String buildUrlWithPaymentId(String baseUrl, Long paymentId) {
        return baseUrl + "?payment_id=" + paymentId;
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("El pago no puede ser null");
        }
        if (payment.getId() == null) {
            throw new IllegalArgumentException("El ID del pago no puede ser null");
        }
    }
}
