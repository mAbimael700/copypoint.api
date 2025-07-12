package com.copypoint.api.infra.mercadopagocheckout.factory;

import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.mercadopago.client.preference.PreferenceItemRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MercadoPagoItemFactory {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoItemFactory.class);

    public List<PreferenceItemRequest> createItemsFromSale(Sale sale, PaymentRequest request) {
        validateSale(sale);
        validatePaymentRequest(request);

        if (Objects.equals(request.amount(), sale.getTotal())) {
            return createItemsFromSaleProfiles(sale);
        } else {
            return createPartialPaymentItem(sale, request);
        }
    }

    private List<PreferenceItemRequest> createItemsFromSaleProfiles(Sale sale) {
        List<PreferenceItemRequest> items = new ArrayList<>();

        for (SaleProfile saleProfile : sale.getSaleProfiles()) {
            PreferenceItemRequest item = createItemFromSaleProfile(saleProfile, sale.getCurrency());
            items.add(item);
        }

        return items;
    }

    private PreferenceItemRequest createItemFromSaleProfile(SaleProfile saleProfile, String currency) {
        String title = saleProfile.getService().getName();
        String description = saleProfile.getProfile().getName();
        Integer quantity = saleProfile.getQuantity();
        BigDecimal unitPrice = BigDecimal.valueOf(saleProfile.getUnitPrice());

        validateItemData(title, quantity, unitPrice, currency);

        logger.debug("Creating item from SaleProfile: title={}, quantity={}, unitPrice={}, currency={}",
                title, quantity, unitPrice, currency);

        return PreferenceItemRequest.builder()
                .title(title.trim())
                .description(description != null ? description.trim() : title.trim())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .currencyId(currency.toUpperCase())
                .build();
    }

    private List<PreferenceItemRequest> createPartialPaymentItem(Sale sale, PaymentRequest request) {
        String title = String.format("Pago parcial - Venta #%d", sale.getId());
        String description = request.description() != null ? request.description() :
                String.format("Pago parcial para venta #%d", sale.getId());

        BigDecimal amount = BigDecimal.valueOf(request.amount());
        String currencyId = request.currency();

        validateItemData(title, 1, amount, currencyId);

        logger.debug("Creating partial payment item: title={}, amount={}, currency={}",
                title, amount, currencyId);

        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .title(title)
                .description(description)
                .quantity(1)
                .unitPrice(amount)
                .currencyId(currencyId.toUpperCase())
                .build();

        return List.of(itemRequest);
    }

    private void validateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("La venta no puede ser null");
        }
        if (sale.getSaleProfiles() == null || sale.getSaleProfiles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un perfil de venta");
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El request de pago no puede ser null");
        }
        if (request.amount() == null || request.amount() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a 0");
        }
        if (request.currency() == null || request.currency().trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
    }

    private void validateItemData(String title, Integer quantity, BigDecimal unitPrice, String currency) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
    }
}
