package com.copypoint.api.infra.exchangerate.http;

import com.copypoint.api.infra.exchangerate.dto.ConversionResponse;
import com.copypoint.api.infra.exchangerate.dto.ExchangeRateResponse;
import com.copypoint.api.infra.exchangerate.dto.SupportedCodesResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.math.BigDecimal;

@HttpExchange
public interface ExchangeRateClient {

    @GetExchange("/{apiKey}/latest/{baseCurrency}")
    ExchangeRateResponse getExchangeRates(@PathVariable("apiKey") String apiKey,
                                          @PathVariable("baseCurrency") String baseCurrency);

    @GetExchange("/{apiKey}/pair/{from}/{to}/{amount}")
    ConversionResponse convertCurrency(@PathVariable("apiKey") String apiKey,
                                       @PathVariable("from") String fromCurrency,
                                       @PathVariable("to") String toCurrency,
                                       @PathVariable("amount") BigDecimal amount);

    @GetExchange("/{apiKey}/codes")
    SupportedCodesResponse getSupportedCodes(@PathVariable("apiKey") String apiKey);

    @GetExchange("/{apiKey}/pair/{from}/{to}")
    ConversionResponse getExchangeRate(@PathVariable("apiKey") String apiKey,
                                       @PathVariable("from") String fromCurrency,
                                       @PathVariable("to") String toCurrency);
}
