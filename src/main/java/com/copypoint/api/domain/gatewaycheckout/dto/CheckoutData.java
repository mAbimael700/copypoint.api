package com.copypoint.api.domain.gatewaycheckout.dto;

import java.time.LocalDateTime;

public record CheckoutData(
        String checkoutUrl,
        String gatewayId,
        String externalReference,
        String sandboxUrl,
        String type,
        LocalDateTime dateCreated,
        String version // Versión del parser usado
) {
}
