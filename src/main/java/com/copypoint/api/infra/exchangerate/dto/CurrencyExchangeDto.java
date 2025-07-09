package com.copypoint.api.infra.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CurrencyExchangeDto(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount,
        LocalDateTime timestamp
) {
}
