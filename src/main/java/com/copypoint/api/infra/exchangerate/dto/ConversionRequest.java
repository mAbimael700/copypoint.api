package com.copypoint.api.infra.exchangerate.dto;

import java.math.BigDecimal;

public record ConversionRequest(
        String fromCurrency,
        String toCurrency,
        BigDecimal amount
) {
}
