package com.copypoint.api.infra.mercadopago.factory;

import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoPayerFactory {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoPayerFactory.class);

    public PreferencePayerRequest createPayerRequest(PaymentRequest request) {
        validatePaymentRequest(request);

        String firstName = request.payer().firstName();
        String lastName = request.payer().lastName();
        String email = request.payer().email();
        String phone = request.payer().phone();
        String identificationType = request.payer().identificationType();
        String identification = request.payer().identification();

        validatePayerData(firstName, lastName, email);

        logger.debug("Creating payer: name={} {}, email={}", firstName, lastName, email);

        return PreferencePayerRequest.builder()
                .name(firstName.trim())
                .surname(lastName.trim())
                .email(email.trim())
                .phone(createPhoneRequest(phone))
                .identification(createIdentificationRequest(identificationType, identification))
                .build();
    }

    private PhoneRequest createPhoneRequest(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return PhoneRequest.builder()
                .number(phone.trim())
                .build();
    }

    private IdentificationRequest createIdentificationRequest(String identificationType, String identification) {
        if (identificationType == null || identificationType.trim().isEmpty() ||
                identification == null || identification.trim().isEmpty()) {
            return null;
        }
        return IdentificationRequest.builder()
                .type(identificationType.trim())
                .number(identification.trim())
                .build();
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El request de pago no puede ser null");
        }
        if (request.payer() == null) {
            throw new IllegalArgumentException("Los datos del pagador son requeridos");
        }
    }

    private void validatePayerData(String firstName, String lastName, String email) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del pagador es requerido");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del pagador es requerido");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del pagador es requerido");
        }
    }
}
